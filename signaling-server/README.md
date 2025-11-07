# Screen Share Signaling Server

WebRTC signaling server for the Android screen sharing application with web dashboard support.

## Features

- WebRTC signaling for Android screen sharing
- **Web Dashboard** for viewing screens from browser
- PIN-based session management
- Socket.IO real-time communication
- Mouse/touch control from web browser

## Installation

```bash
npm install
```

## Running the Server

```bash
npm start
```

The server will run on port 3000 by default. You can change the port by setting the PORT environment variable:

```bash
PORT=8080 npm start
```

Once started, you can:
- Access the web dashboard at: `http://localhost:3000`
- View server status at: `http://localhost:3000/status`

## Web Dashboard

The server includes a built-in web dashboard that allows you to view Android screens from any modern web browser.

### How to Use Web Dashboard

1. Start the signaling server (`npm start`)
2. Open your browser and go to `http://localhost:3000` (or your server IP)
3. On your Android device, open the Screen Share app and tap "Share Screen"
4. Note the 6-digit PIN displayed on the Android device
5. Enter the PIN in the web dashboard and click "Connect"
6. You can now view and control the Android screen from your browser!

### Web Dashboard Features

- ✅ Real-time video streaming from Android devices
- ✅ Mouse control (click and drag on the video)
- ✅ Connection statistics display
- ✅ Responsive design (works on desktop, tablet, mobile)
- ✅ Clean Material Design UI
- ✅ Toggle mouse control on/off

## Configuration for Android App

Update the server URL in your Android app to point to this server:
- For local testing with emulator: `http://10.0.2.2:3000`
- For local testing with real device: `http://YOUR_COMPUTER_IP:3000`
- For production: `https://YOUR_DOMAIN.com`

## API Endpoints

- `GET /` - Web Dashboard (HTML interface)
- `GET /status` - View active sessions (JSON)

## Socket.IO Events

### Client to Server
- `create-session` - Host creates a new session with PIN
- `join-session` - Viewer joins a session with PIN
- `offer` - WebRTC offer
- `answer` - WebRTC answer
- `ice-candidate` - ICE candidate exchange
- `touch-event` - Touch events from viewer
- `end-session` - End the current session

### Server to Client
- `session-created` - Session successfully created
- `session-joined` - Successfully joined session
- `viewer-joined` - Viewer has joined (sent to host)
- `offer` - WebRTC offer (forwarded to viewer)
- `answer` - WebRTC answer (forwarded to host)
- `ice-candidate` - ICE candidate (forwarded to peer)
- `touch-event` - Touch event (forwarded to host)
- `host-disconnected` - Host has disconnected
- `viewer-disconnected` - Viewer has disconnected
- `session-ended` - Session has been ended
- `error` - Error message
