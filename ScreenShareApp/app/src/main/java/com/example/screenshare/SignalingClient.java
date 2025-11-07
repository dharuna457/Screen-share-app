package com.example.screenshare;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import java.net.URISyntaxException;

public class SignalingClient {
    private static final String TAG = "SignalingClient";

    // IMPORTANT: Change this to your server URL
    // For Android Emulator: http://10.0.2.2:3000
    // For real device: http://YOUR_COMPUTER_IP:3000
    private static final String SERVER_URL = "https://rtc.dharunashokkumar.com/";

    private Socket socket;
    private SignalingEvents events;
    private Gson gson = new Gson();

    public interface SignalingEvents {
        void onSessionCreated(String pin);
        void onSessionJoined(String pin);
        void onViewerJoined(String viewerId);
        void onOfferReceived(SessionDescription offer);
        void onAnswerReceived(SessionDescription answer);
        void onIceCandidateReceived(IceCandidate candidate);
        void onError(String message);
        void onHostDisconnected();
        void onViewerDisconnected();
        void onSessionEnded();
        void onTouchEvent(float x, float y, String action);
    }

    public SignalingClient(SignalingEvents events) {
        this.events = events;
        try {
            socket = IO.socket(SERVER_URL);
            setupSocketListeners();
        } catch (URISyntaxException e) {
            Log.e(TAG, "Socket connection error", e);
        }
    }

    private void setupSocketListeners() {
        socket.on(Socket.EVENT_CONNECT, args -> {
            Log.d(TAG, "Socket connected");
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            Log.d(TAG, "Socket disconnected");
        });

        socket.on("session-created", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String pin = data.getString("pin");
                events.onSessionCreated(pin);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing session-created", e);
            }
        });

        socket.on("session-joined", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String pin = data.getString("pin");
                events.onSessionJoined(pin);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing session-joined", e);
            }
        });

        socket.on("viewer-joined", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String viewerId = data.getString("viewerId");
                events.onViewerJoined(viewerId);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing viewer-joined", e);
            }
        });

        socket.on("offer", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                JSONObject offerJson = data.getJSONObject("offer");
                SessionDescription offer = new SessionDescription(
                        SessionDescription.Type.OFFER,
                        offerJson.getString("sdp")
                );
                events.onOfferReceived(offer);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing offer", e);
            }
        });

        socket.on("answer", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                JSONObject answerJson = data.getJSONObject("answer");
                SessionDescription answer = new SessionDescription(
                        SessionDescription.Type.ANSWER,
                        answerJson.getString("sdp")
                );
                events.onAnswerReceived(answer);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing answer", e);
            }
        });

        socket.on("ice-candidate", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                JSONObject candidateJson = data.getJSONObject("candidate");
                IceCandidate candidate = new IceCandidate(
                        candidateJson.getString("sdpMid"),
                        candidateJson.getInt("sdpMLineIndex"),
                        candidateJson.getString("sdp")
                );
                events.onIceCandidateReceived(candidate);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing ice-candidate", e);
            }
        });

        socket.on("error", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String message = data.getString("message");
                events.onError(message);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing error message", e);
            }
        });

        socket.on("host-disconnected", args -> {
            events.onHostDisconnected();
        });

        socket.on("viewer-disconnected", args -> {
            events.onViewerDisconnected();
        });

        socket.on("session-ended", args -> {
            events.onSessionEnded();
        });

        socket.on("touch-event", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                float x = (float) data.getDouble("x");
                float y = (float) data.getDouble("y");
                String action = data.getString("action");
                events.onTouchEvent(x, y, action);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing touch-event", e);
            }
        });
    }

    public void connect() {
        if (!socket.connected()) {
            socket.connect();
        }
    }

    public void disconnect() {
        if (socket.connected()) {
            socket.disconnect();
        }
    }

    public void createSession(String pin) {
        try {
            JSONObject data = new JSONObject();
            data.put("pin", pin);
            socket.emit("create-session", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating session", e);
        }
    }

    public void joinSession(String pin) {
        try {
            JSONObject data = new JSONObject();
            data.put("pin", pin);
            socket.emit("join-session", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error joining session", e);
        }
    }

    public void sendOffer(String pin, SessionDescription offer) {
        try {
            JSONObject offerJson = new JSONObject();
            offerJson.put("type", "offer");
            offerJson.put("sdp", offer.description);

            JSONObject data = new JSONObject();
            data.put("pin", pin);
            data.put("offer", offerJson);

            socket.emit("offer", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error sending offer", e);
        }
    }

    public void sendAnswer(String pin, SessionDescription answer) {
        try {
            JSONObject answerJson = new JSONObject();
            answerJson.put("type", "answer");
            answerJson.put("sdp", answer.description);

            JSONObject data = new JSONObject();
            data.put("pin", pin);
            data.put("answer", answerJson);

            socket.emit("answer", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error sending answer", e);
        }
    }

    public void sendIceCandidate(String pin, IceCandidate candidate) {
        try {
            JSONObject candidateJson = new JSONObject();
            candidateJson.put("sdpMid", candidate.sdpMid);
            candidateJson.put("sdpMLineIndex", candidate.sdpMLineIndex);
            candidateJson.put("sdp", candidate.sdp);

            JSONObject data = new JSONObject();
            data.put("pin", pin);
            data.put("candidate", candidateJson);

            socket.emit("ice-candidate", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error sending ice candidate", e);
        }
    }

    public void sendTouchEvent(String pin, float x, float y, String action) {
        try {
            JSONObject data = new JSONObject();
            data.put("pin", pin);
            data.put("x", x);
            data.put("y", y);
            data.put("action", action);

            socket.emit("touch-event", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error sending touch event", e);
        }
    }

    public void endSession() {
        socket.emit("end-session");
    }
}
