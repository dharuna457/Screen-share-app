package com.example.screenshare;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import java.util.Random;

public class ShareScreenActivity extends AppCompatActivity implements
        SignalingClient.SignalingEvents, WebRTCClient.WebRTCEvents {

    private static final String TAG = "ShareScreenActivity";
    private static final int SCREEN_CAPTURE_REQUEST_CODE = 1;

    private TextView pinTextView;
    private TextView statusTextView;
    private ProgressBar progressBar;
    private MaterialButton stopButton;

    private String pin;
    private SignalingClient signalingClient;
    private WebRTCClient webRTCClient;
    private MediaProjectionManager projectionManager;
    private Intent mediaProjectionPermissionResultData;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_screen);

        pinTextView = findViewById(R.id.pinTextView);
        statusTextView = findViewById(R.id.statusTextView);
        progressBar = findViewById(R.id.progressBar);
        stopButton = findViewById(R.id.stopButton);

        // Generate random PIN
        pin = generatePin();
        pinTextView.setText(pin);

        // Don't initialize SignalingClient yet - will do after WebRTC is ready
        // to avoid Socket.IO conflicting with WebRTC native threads

        // Request screen capture permission
        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREEN_CAPTURE_REQUEST_CODE);

        stopButton.setOnClickListener(v -> stopSharing());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                mediaProjectionPermissionResultData = data;

                // Start foreground service
                Intent serviceIntent = new Intent(this, ScreenCaptureService.class);
                startService(serviceIntent);

                // Wait for service to start, then initialize WebRTC
                // MediaProjection requires the foreground service to be running
                new android.os.Handler().postDelayed(() -> {
                    try {
                        Log.d(TAG, "Starting WebRTC initialization...");
                        initializeWebRTC();
                        Log.d(TAG, "WebRTC initialized successfully!");

                        // Initialize Socket.IO after WebRTC is ready
                        signalingClient = new SignalingClient(this);
                        signalingClient.connect();
                        signalingClient.createSession(pin);

                        statusTextView.setText(R.string.waiting_connection);
                    } catch (Exception e) {
                        Log.e(TAG, "Initialization failed", e);
                        statusTextView.setText("Error: " + e.getMessage());
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }, 1000); // 1 second delay to ensure foreground service is running

            } else {
                Toast.makeText(this, "Screen capture permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initializeWebRTC() {
        webRTCClient = new WebRTCClient(this, this);
        webRTCClient.initializePeerConnection();

        // Start screen capture
        ScreenCapturerAndroid screenCapturer = new ScreenCapturerAndroid(
                mediaProjectionPermissionResultData,
                new MediaProjection.Callback() {
                    @Override
                    public void onStop() {
                        super.onStop();
                        runOnUiThread(() -> stopSharing());
                    }
                }
        );

        webRTCClient.startLocalVideoCapture(screenCapturer);
    }

    private String generatePin() {
        Random random = new Random();
        int pinNumber = 100000 + random.nextInt(900000);
        return String.valueOf(pinNumber);
    }

    private void stopSharing() {
        if (signalingClient != null) {
            signalingClient.endSession();
            signalingClient.disconnect();
        }

        if (webRTCClient != null) {
            webRTCClient.close();
        }

        Intent serviceIntent = new Intent(this, ScreenCaptureService.class);
        stopService(serviceIntent);

        finish();
    }

    // SignalingClient.SignalingEvents implementation
    @Override
    public void onSessionCreated(String pin) {
        runOnUiThread(() -> {
            Log.d(TAG, "Session created with PIN: " + pin);
            statusTextView.setText(R.string.waiting_connection);
        });
    }

    @Override
    public void onSessionJoined(String pin) {
        // Not used in share screen
    }

    @Override
    public void onViewerJoined(String viewerId) {
        runOnUiThread(() -> {
            Log.d(TAG, "Viewer joined: " + viewerId);
            statusTextView.setText("Viewer connected. Establishing connection...");
            progressBar.setVisibility(View.VISIBLE);

            // Create and send offer
            webRTCClient.createOffer();
        });
    }

    @Override
    public void onOfferReceived(SessionDescription offer) {
        // Not used in share screen
    }

    @Override
    public void onAnswerReceived(SessionDescription answer) {
        runOnUiThread(() -> {
            Log.d(TAG, "Answer received");
            webRTCClient.setRemoteDescription(answer);
        });
    }

    @Override
    public void onIceCandidateReceived(IceCandidate candidate) {
        runOnUiThread(() -> {
            Log.d(TAG, "ICE candidate received");
            webRTCClient.addIceCandidate(candidate);
        });
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error: " + message);
        });
    }

    @Override
    public void onHostDisconnected() {
        // Not used in share screen
    }

    @Override
    public void onViewerDisconnected() {
        runOnUiThread(() -> {
            statusTextView.setText(R.string.waiting_connection);
            progressBar.setVisibility(View.VISIBLE);
            isConnected = false;
        });
    }

    @Override
    public void onSessionEnded() {
        runOnUiThread(this::stopSharing);
    }

    @Override
    public void onTouchEvent(float x, float y, String action) {
        Log.d(TAG, "Touch event received: x=" + x + ", y=" + y + ", action=" + action);
        // Note: Simulating touch events on Android requires either:
        // 1. Root access
        // 2. Accessibility Service
        // 3. Using "input tap" via ADB shell commands
        // For now, we'll use ADB shell commands which requires shell permissions

        runOnUiThread(() -> {
            try {
                // Get screen dimensions
                android.view.Display display = getWindowManager().getDefaultDisplay();
                android.graphics.Point size = new android.graphics.Point();
                display.getRealSize(size);

                // Convert normalized coordinates to actual screen coordinates
                int screenX = (int) (x * size.x);
                int screenY = (int) (y * size.y);

                // Execute touch using input command (requires shell permissions)
                String cmd = "";
                if ("DOWN".equals(action)) {
                    cmd = "input touchscreen swipe " + screenX + " " + screenY + " " + screenX + " " + screenY + " 100";
                } else if ("UP".equals(action)) {
                    // Tap gesture
                    cmd = "input tap " + screenX + " " + screenY;
                }

                if (!cmd.isEmpty()) {
                    Runtime.getRuntime().exec(cmd);
                    Log.d(TAG, "Executed touch command: " + cmd);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error simulating touch", e);
            }
        });
    }

    // WebRTCClient.WebRTCEvents implementation
    @Override
    public void onIceCandidate(IceCandidate candidate) {
        Log.d(TAG, "Local ICE candidate: " + candidate);
        signalingClient.sendIceCandidate(pin, candidate);
    }

    @Override
    public void onAddStream(MediaStream stream) {
        // Not used in share screen
    }

    @Override
    public void onConnectionChange(PeerConnection.IceConnectionState state) {
        runOnUiThread(() -> {
            Log.d(TAG, "Connection state: " + state);
            switch (state) {
                case CONNECTED:
                    statusTextView.setText(R.string.connected);
                    progressBar.setVisibility(View.GONE);
                    isConnected = true;
                    break;
                case DISCONNECTED:
                case FAILED:
                    statusTextView.setText("Connection lost");
                    progressBar.setVisibility(View.GONE);
                    isConnected = false;
                    break;
                case CHECKING:
                    statusTextView.setText(R.string.connecting);
                    progressBar.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    @Override
    public void onOfferCreated(SessionDescription offer) {
        Log.d(TAG, "Offer created, sending to signaling server");
        signalingClient.sendOffer(pin, offer);
    }

    @Override
    public void onAnswerCreated(SessionDescription answer) {
        Log.d(TAG, "Answer created, sending to signaling server");
        signalingClient.sendAnswer(pin, answer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSharing();
    }
}
