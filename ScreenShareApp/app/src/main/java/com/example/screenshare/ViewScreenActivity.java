package com.example.screenshare;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

public class ViewScreenActivity extends AppCompatActivity implements
        SignalingClient.SignalingEvents, WebRTCClient.WebRTCEvents {

    private static final String TAG = "ViewScreenActivity";

    private LinearLayout pinInputLayout;
    private RelativeLayout remoteViewLayout;
    private TextInputEditText pinEditText;
    private MaterialButton connectButton;
    private MaterialButton disconnectButton;
    private TextView statusTextView;
    private SurfaceViewRenderer remoteVideoView;

    private String pin;
    private SignalingClient signalingClient;
    private WebRTCClient webRTCClient;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_screen);

        pinInputLayout = findViewById(R.id.pinInputLayout);
        remoteViewLayout = findViewById(R.id.remoteViewLayout);
        pinEditText = findViewById(R.id.pinEditText);
        connectButton = findViewById(R.id.connectButton);
        disconnectButton = findViewById(R.id.disconnectButton);
        statusTextView = findViewById(R.id.statusTextView);
        remoteVideoView = findViewById(R.id.remoteVideoView);

        // Initialize signaling client
        signalingClient = new SignalingClient(this);

        connectButton.setOnClickListener(v -> {
            String enteredPin = pinEditText.getText().toString().trim();
            if (enteredPin.length() == 6) {
                connectToSession(enteredPin);
            } else {
                Toast.makeText(this, "Please enter a valid 6-digit PIN", Toast.LENGTH_SHORT).show();
            }
        });

        disconnectButton.setOnClickListener(v -> disconnect());

        // Setup touch listener for remote control
        setupTouchControl();
    }

    private void connectToSession(String pin) {
        this.pin = pin;
        statusTextView.setText(R.string.connecting);

        // Initialize WebRTC
        webRTCClient = new WebRTCClient(this, this);
        webRTCClient.initializePeerConnection();
        webRTCClient.initSurfaceView(remoteVideoView);

        // Connect to signaling server and join session
        signalingClient.connect();
        signalingClient.joinSession(pin);
    }

    private void disconnect() {
        if (signalingClient != null) {
            signalingClient.endSession();
            signalingClient.disconnect();
        }

        if (webRTCClient != null) {
            webRTCClient.close();
        }

        finish();
    }

    private void setupTouchControl() {
        remoteVideoView.setOnTouchListener((v, event) -> {
            if (!isConnected) {
                return false;
            }

            float x = event.getX() / v.getWidth();
            float y = event.getY() / v.getHeight();

            String action;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    action = "DOWN";
                    break;
                case MotionEvent.ACTION_MOVE:
                    action = "MOVE";
                    break;
                case MotionEvent.ACTION_UP:
                    action = "UP";
                    break;
                default:
                    return false;
            }

            signalingClient.sendTouchEvent(pin, x, y, action);
            return true;
        });
    }

    // SignalingClient.SignalingEvents implementation
    @Override
    public void onSessionCreated(String pin) {
        // Not used in viewer
    }

    @Override
    public void onSessionJoined(String pin) {
        runOnUiThread(() -> {
            Log.d(TAG, "Session joined: " + pin);
            statusTextView.setText("Joined session. Waiting for host...");
        });
    }

    @Override
    public void onViewerJoined(String viewerId) {
        // Not used in viewer
    }

    @Override
    public void onOfferReceived(SessionDescription offer) {
        runOnUiThread(() -> {
            Log.d(TAG, "Offer received");
            webRTCClient.setRemoteDescription(offer);
            webRTCClient.createAnswer();
        });
    }

    @Override
    public void onAnswerReceived(SessionDescription answer) {
        // Not used in viewer
    }

    @Override
    public void onIceCandidateReceived(IceCandidate candidate) {
        runOnUiThread(() -> {
            Log.d(TAG, "ICE candidate received");
            if (webRTCClient != null) {
                webRTCClient.addIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
            statusTextView.setText("Error: " + message);
            Log.e(TAG, "Error: " + message);
        });
    }

    @Override
    public void onHostDisconnected() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Host disconnected", Toast.LENGTH_SHORT).show();
            disconnect();
        });
    }

    @Override
    public void onViewerDisconnected() {
        // Not used in viewer
    }

    @Override
    public void onSessionEnded() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Session ended by host", Toast.LENGTH_SHORT).show();
            disconnect();
        });
    }

    @Override
    public void onTouchEvent(float x, float y, String action) {
        // Not used in viewer
    }

    // WebRTCClient.WebRTCEvents implementation
    @Override
    public void onIceCandidate(IceCandidate candidate) {
        Log.d(TAG, "Local ICE candidate: " + candidate);
        signalingClient.sendIceCandidate(pin, candidate);
    }

    @Override
    public void onAddStream(MediaStream stream) {
        runOnUiThread(() -> {
            Log.d(TAG, "Remote stream added");
            if (stream.videoTracks.size() > 0) {
                VideoTrack remoteVideoTrack = stream.videoTracks.get(0);
                remoteVideoTrack.addSink(remoteVideoView);

                // Switch to video view
                pinInputLayout.setVisibility(View.GONE);
                remoteViewLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onConnectionChange(PeerConnection.IceConnectionState state) {
        runOnUiThread(() -> {
            Log.d(TAG, "Connection state: " + state);
            switch (state) {
                case CONNECTED:
                    statusTextView.setText(R.string.connected);
                    isConnected = true;
                    break;
                case DISCONNECTED:
                case FAILED:
                    statusTextView.setText("Connection lost");
                    isConnected = false;
                    Toast.makeText(this, "Connection lost", Toast.LENGTH_SHORT).show();
                    break;
                case CHECKING:
                    statusTextView.setText(R.string.connecting);
                    break;
            }
        });
    }

    @Override
    public void onOfferCreated(SessionDescription offer) {
        // Not used in viewer
    }

    @Override
    public void onAnswerCreated(SessionDescription answer) {
        Log.d(TAG, "Answer created, sending to signaling server");
        signalingClient.sendAnswer(pin, answer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }
}
