# Android Screen Share App

A real-time screen sharing application for Android that allows you to share your screen with another device using WebRTC technology.

## Features

- Share your screen with a simple PIN code
- View and control remote screens
- Real-time video streaming using WebRTC
- Touch control for remote interaction
- Clean Material Design UI
- Works on Android 5.0 (API 21) and above

## Architecture

- **Android App (Java)**: Native Android application with WebRTC support
- **Signaling Server (Node.js)**: Manages PIN-to-session mapping and WebRTC signaling
- **WebRTC**: Peer-to-peer video streaming

## Project Structure

```
ScreenShareApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/screenshare/
│   │   │   ├── MainActivity.java                 # Main entry point
│   │   │   ├── ShareScreenActivity.java          # Screen sharing activity
│   │   │   ├── ViewScreenActivity.java           # Remote viewing activity
│   │   │   ├── WebRTCClient.java                 # WebRTC peer connection management
│   │   │   ├── SignalingClient.java              # Socket.IO signaling client
│   │   │   └── ScreenCaptureService.java         # Foreground service for screen capture
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── activity_share_screen.xml
│   │   │   │   └── activity_view_screen.xml
│   │   │   └── values/
│   │   │       ├── strings.xml
│   │   │       └── colors.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle
└── build.gradle

signaling-server/
├── server.js              # Node.js signaling server
├── package.json
└── README.md
```

## Setup Instructions

### 1. Setup Signaling Server

```bash
cd signaling-server
npm install
npm start
```

The server will run on `http://localhost:3000` by default.

### 2. Configure Android App

Open `SignalingClient.java` and update the `SERVER_URL`:

```java
// For Android Emulator (server running on your computer)
private static final String SERVER_URL = "http://10.0.2.2:3000";

// For real Android device (server running on your computer)
private static final String SERVER_URL = "http://YOUR_COMPUTER_IP:3000";

// For production deployment
private static final String SERVER_URL = "http://YOUR_SERVER_URL:3000";
```

**Finding your computer's IP:**
- **Windows**: Open CMD and run `ipconfig`, look for IPv4 Address
- **macOS/Linux**: Open Terminal and run `ifconfig` or `ip addr`, look for inet address

### 3. Build and Install Android App

**Option A: Using Android Studio**
1. Open the `ScreenShareApp` folder in Android Studio
2. Wait for Gradle sync to complete
3. Connect your Android device or start an emulator
4. Click Run (Green play button)

**Option B: Using Command Line**
```bash
cd ScreenShareApp
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## How to Use

### Sharing Your Screen

1. Open the app and tap **"Share Screen"**
2. Grant the screen capture permission
3. A 6-digit PIN will be displayed
4. Share this PIN with the person who wants to view your screen
5. Wait for the viewer to connect
6. Your screen is now being shared!
7. Tap **"Stop Sharing"** when done

### Viewing a Remote Screen

1. Open the app and tap **"View Screen"**
2. Enter the 6-digit PIN provided by the host
3. Tap **"Connect"**
4. Wait for the connection to establish
5. You can now see the remote screen
6. Touch the screen to control the remote device (if implemented)
7. Tap **"Disconnect"** when done

## Permissions Required

The app requires the following permissions:
- **INTERNET**: For network communication
- **ACCESS_NETWORK_STATE**: To check network availability
- **RECORD_AUDIO**: For audio streaming (optional)
- **FOREGROUND_SERVICE**: To keep screen sharing active
- **FOREGROUND_SERVICE_MEDIA_PROJECTION**: For screen capture service

## Technical Details

### WebRTC Configuration

- **STUN Server**: `stun:stun.l.google.com:19302`
- **Video Resolution**: 1280x720 @ 30fps
- **Codec**: VP8/VP9 (hardware accelerated when available)

### Signaling Protocol

The app uses Socket.IO for WebRTC signaling with the following events:
- `create-session`: Host creates a session with PIN
- `join-session`: Viewer joins with PIN
- `offer`: WebRTC offer from host
- `answer`: WebRTC answer from viewer
- `ice-candidate`: ICE candidate exchange
- `touch-event`: Remote touch control events

### Security Considerations

- PIN codes are 6-digit random numbers (1 million possibilities)
- Connections use WebRTC encryption (DTLS-SRTP)
- Sessions are automatically cleaned up on disconnect
- Consider implementing PIN expiration for production use
- Use HTTPS/WSS for production signaling server

## Troubleshooting

### Connection Issues

1. **"Invalid PIN" error**
   - Ensure the PIN is correct
   - Make sure the host started sharing before viewer connects
   - Check that both devices can reach the signaling server

2. **Video not showing**
   - Check that screen capture permission was granted
   - Verify both devices are connected to the signaling server
   - Check network connectivity

3. **Can't connect from real device**
   - Ensure your computer and phone are on the same network
   - Update SERVER_URL with your computer's correct IP address
   - Check firewall settings

### Testing with Android Emulator

- Use two emulators or one emulator + one real device
- Emulator URL: `http://10.0.2.2:3000`
- Real device URL: `http://YOUR_COMPUTER_IP:3000`

## Production Deployment

For production use:

1. **Deploy Signaling Server**
   - Use a cloud service (AWS, Google Cloud, Heroku, DigitalOcean)
   - Enable HTTPS/WSS
   - Set up proper logging and monitoring
   - Implement rate limiting

2. **Android App Updates**
   - Update SERVER_URL to production server
   - Implement user authentication
   - Add PIN expiration
   - Enable ProGuard/R8 for code optimization
   - Add crash reporting (Firebase Crashlytics)

3. **Security Enhancements**
   - Implement session timeout
   - Add PIN expiration (e.g., 5 minutes)
   - Rate limit PIN attempts
   - Add user authentication
   - Use TURN servers for better connectivity

## Known Limitations

- Only one viewer per session
- No persistent sessions (disconnection ends session)
- Touch control is basic (no multi-touch, gestures)
- No audio streaming by default
- Works best on same local network

## Future Enhancements

- [ ] Multiple viewers per session
- [ ] Audio streaming support
- [ ] Recording functionality
- [ ] Session history
- [ ] User accounts and authentication
- [ ] End-to-end encryption
- [ ] Better touch control (multi-touch, gestures)
- [ ] Chat functionality
- [ ] File sharing

## Dependencies

### Android App
- WebRTC: `org.webrtc:google-webrtc:1.0.32006`
- Socket.IO Client: `io.socket:socket.io-client:2.1.0`
- Material Components: `com.google.android.material:material:1.11.0`
- AndroidX AppCompat: `androidx.appcompat:appcompat:1.6.1`
- Gson: `com.google.code.gson:gson:2.10.1`

### Signaling Server
- Express: `^4.18.2`
- Socket.IO: `^4.6.1`

## License

This is a sample project for educational purposes.

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review server logs for signaling issues
3. Check Android logcat for app issues
4. Ensure all dependencies are properly installed

## Credits

Built with:
- Android SDK
- WebRTC
- Socket.IO
- Node.js
- Material Design Components
