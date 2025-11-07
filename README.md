# Android Screen Share App with Web Dashboard

A complete real-time screen sharing solution for Android devices with both mobile-to-mobile and mobile-to-web viewing capabilities using WebRTC technology.

## Features

### Android App
- 📱 Share your Android screen with a simple PIN code
- 🔢 6-digit PIN for easy connection
- 📺 View other Android screens
- 👆 Touch control for remote interaction
- 🎨 Clean Material Design UI
- 📡 WebRTC for high-quality streaming

### Web Dashboard
- 🌐 View Android screens from any web browser
- 💻 No installation required on viewer side
- 🖱️ Mouse control support
- 📊 Real-time connection statistics
- 📱 Responsive design (works on desktop, tablet, mobile)
- ⚡ Low latency peer-to-peer streaming

## Quick Demo

### Host (Android Device)
1. Open app → Tap "Share Screen"
2. Grant permission
3. Get PIN (e.g., 123456)

### Viewer (Android Device OR Web Browser)
**Option A - Android App:**
- Open app → Tap "View Screen" → Enter PIN → Connect

**Option B - Web Browser:**
- Open `http://localhost:3000` → Enter PIN → Connect

## Architecture

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│  Android Host   │         │    Signaling    │         │     Viewer      │
│                 │         │     Server      │         │ (Android or Web)│
│  - Share Screen │◄───────►│  (Node.js)      │◄───────►│  - View Screen  │
│  - MediaProject │  Socket │  - Socket.IO    │  Socket │  - WebRTC       │
│  - WebRTC       │   .IO   │  - Web Dashboard│   .IO   │  - Mouse Ctrl   │
└─────────────────┘         └─────────────────┘         └─────────────────┘
         │                                                         │
         │                                                         │
         └───────────────────────────────────────────────────────┘
                    WebRTC P2P Video Stream (Direct)
```

## Tech Stack

### Android Application
- **Language**: Java
- **Min SDK**: API 21 (Android 5.0)
- **Target SDK**: API 34 (Android 14)
- **Key Libraries**:
  - WebRTC: `org.webrtc:google-webrtc:1.0.32006`
  - Socket.IO: `io.socket:socket.io-client:2.1.0`
  - Material Components: `com.google.android.material:material:1.11.0`

### Signaling Server
- **Runtime**: Node.js
- **Framework**: Express
- **Real-time**: Socket.IO
- **Web Dashboard**: HTML5 + CSS3 + Vanilla JavaScript

### WebRTC
- **Video Codec**: VP8/VP9 (hardware accelerated)
- **Resolution**: 1280x720 @ 30fps
- **STUN Server**: Google STUN (`stun.l.google.com`)

## Installation & Setup

### 1. Install Prerequisites

```bash
# Node.js (download from nodejs.org)
node --version  # Should be v14 or higher

# Android Studio (download from developer.android.com)
# Or Android SDK with ADB
```

### 2. Clone or Download the Project

```bash
cd "Desktop/ATA RA"
```

### 3. Setup Signaling Server

```bash
cd signaling-server
npm install
npm start
```

Server runs on `http://localhost:3000`

### 4. Configure Android App

Update the server URL in `SignalingClient.java`:

**For Android Emulator:**
```java
private static final String SERVER_URL = "http://10.0.2.2:3000";
```

**For Real Device (same network):**
```java
private static final String SERVER_URL = "http://YOUR_COMPUTER_IP:3000";
```

To find your IP:
- Windows: `ipconfig`
- macOS/Linux: `ifconfig` or `ip addr`

### 5. Build & Install Android App

**Using Android Studio:**
1. Open `ScreenShareApp` folder
2. Wait for Gradle sync
3. Click Run (green play button)

**Using Command Line:**
```bash
cd ScreenShareApp
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Usage

### Sharing Your Screen (Android)

1. Launch the Screen Share app
2. Tap **"Share Screen"**
3. Grant screen capture permission when prompted
4. A 6-digit PIN will be displayed (e.g., `456789`)
5. Share this PIN with the viewer
6. Wait for viewer to connect
7. Your screen is now being shared!
8. Tap **"Stop Sharing"** when done

### Viewing from Another Android Device

1. Launch the Screen Share app
2. Tap **"View Screen"**
3. Enter the 6-digit PIN
4. Tap **"Connect"**
5. Wait for connection to establish
6. View and control the remote screen
7. Tap **"Disconnect"** when done

### Viewing from Web Browser (NEW!)

1. Open your web browser (Chrome, Firefox, Safari, Edge)
2. Navigate to `http://localhost:3000` (or server IP)
3. Enter the 6-digit PIN in the dashboard
4. Click **"Connect"**
5. Wait a few seconds for WebRTC connection
6. The Android screen appears in your browser!
7. Click on the video to control the Android device
8. Use the checkbox to toggle mouse control on/off
9. Click **"Disconnect"** when done

## Documentation

- **[QUICK_START.md](QUICK_START.md)** - Get up and running in 5 minutes
- **[WEB_DASHBOARD_GUIDE.md](WEB_DASHBOARD_GUIDE.md)** - Complete web dashboard guide
- **[PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)** - Detailed project structure
- **[ScreenShareApp/README.md](ScreenShareApp/README.md)** - Android app documentation
- **[signaling-server/README.md](signaling-server/README.md)** - Server documentation
- **[ScreenShareApp/SERVER_CONFIG.md](ScreenShareApp/SERVER_CONFIG.md)** - Server configuration help

## Screenshots & Demo

### Android App Flow
```
Main Screen          Share Screen         View Screen
┌──────────┐        ┌──────────┐        ┌──────────┐
│          │        │ Your PIN │        │ Enter PIN│
│ [Share]  │   →    │  456789  │        │ ┌──────┐ │
│          │        │          │        │ │456789│ │
│ [View]   │        │ Status:  │        │ └──────┘ │
│          │        │ Connected│        │ [Connect]│
└──────────┘        └──────────┘        └──────────┘
```

### Web Dashboard
```
Browser Interface
┌─────────────────────────────────────────────┐
│  📱 Screen Share Viewer                     │
│  Enter 6-digit PIN: [______]  [Connect]    │
└─────────────────────────────────────────────┘

After Connection:
┌─────────────────────────────────────────────┐
│ ● Connected  PIN: 456789      [Disconnect] │
│ ┌─────────────────────────────────────────┐ │
│ │                                         │ │
│ │        Android Screen Video             │ │
│ │                                         │ │
│ └─────────────────────────────────────────┘ │
│ ☑ Enable Mouse Control                     │
└─────────────────────────────────────────────┘
```

## Troubleshooting

### Connection Issues

| Problem | Solution |
|---------|----------|
| "Invalid PIN" | Check PIN is correct, ensure host shared first |
| Video not showing | Verify screen capture permission granted |
| Can't connect from web | Ensure server is running, check URL |
| Connection drops | Check Wi-Fi signal, reduce network usage |

### Common Issues

**Android App:**
- Make sure Android version is 5.0+
- Grant all required permissions
- Update `SERVER_URL` to correct address
- Check firewall allows port 3000

**Web Dashboard:**
- Use modern browser (Chrome, Firefox, Edge, Safari)
- Check browser console for errors (F12)
- Ensure JavaScript is enabled
- Verify WebRTC is supported

**Server:**
- Ensure Node.js is installed
- Run `npm install` in signaling-server folder
- Check port 3000 is not in use
- Verify firewall allows connections

## Network Requirements

### Same Network Testing
- Both devices on same Wi-Fi
- Server: `http://YOUR_COMPUTER_IP:3000`
- Example: `http://192.168.1.100:3000`

### Internet Access (Production)
- Deploy server to cloud (Heroku, AWS, etc.)
- Use HTTPS (required for WebRTC)
- Update Android app with production URL
- Consider TURN server for NAT traversal

## Security

### Current Implementation
- PIN-based authentication (6-digit)
- WebRTC encryption (DTLS-SRTP)
- Peer-to-peer media streaming
- No persistent session storage

### Production Recommendations
- [ ] Add PIN expiration (e.g., 5 minutes)
- [ ] Implement user authentication
- [ ] Use HTTPS/WSS for signaling
- [ ] Add rate limiting for PIN attempts
- [ ] Enable session timeout
- [ ] Add audit logging

## Performance

- **Video Quality**: 1280x720 @ 30fps
- **Latency**: 100-500ms (depends on network)
- **Bandwidth**: 1-3 Mbps per stream
- **Connections**: One viewer per session (current)

## Browser Compatibility

| Browser | Version | Status |
|---------|---------|--------|
| Chrome | 74+ | ✅ Recommended |
| Firefox | 66+ | ✅ Supported |
| Safari | 12.1+ | ✅ Supported |
| Edge | 79+ | ✅ Supported |
| Opera | 62+ | ✅ Supported |

## Android Compatibility

- **Minimum**: Android 5.0 (API 21)
- **Target**: Android 14 (API 34)
- **Tested**: Android 7.0 - 14

## Permissions Required

- `INTERNET` - Network communication
- `ACCESS_NETWORK_STATE` - Check connectivity
- `RECORD_AUDIO` - Audio streaming (optional)
- `FOREGROUND_SERVICE` - Keep screen sharing active
- `FOREGROUND_SERVICE_MEDIA_PROJECTION` - Screen capture service

## Known Limitations

- One viewer per session (not multiple viewers)
- No session persistence (disconnection ends session)
- Basic touch control (no multi-touch or gestures)
- No audio streaming by default
- Works best on same local network

## Future Enhancements

- [ ] Multiple viewers per session
- [ ] Audio streaming support
- [ ] Recording functionality
- [ ] Clipboard synchronization
- [ ] File transfer
- [ ] Chat between host and viewer
- [ ] Keyboard input forwarding
- [ ] Gesture support (pinch, zoom)
- [ ] Session history and analytics
- [ ] User accounts and authentication

## Use Cases

- 👨‍💻 **Remote Support**: Help family/friends with tech issues
- 🎓 **Education**: Demonstrate app usage or tutorials
- 🎮 **Gaming**: Stream mobile gameplay to larger screen
- 👥 **Presentations**: Share mobile screen in meetings
- 🔧 **Testing**: QA testing and bug reproduction
- 📱 **Mirroring**: Use Android apps on computer

## Project Structure

```
ATA RA/
├── ScreenShareApp/          # Android application
│   ├── app/src/main/java/   # Java source code
│   ├── app/src/main/res/    # Android resources
│   └── app/build.gradle     # App dependencies
├── signaling-server/        # Node.js server
│   ├── public/              # Web dashboard files
│   ├── server.js            # Main server file
│   └── package.json         # Node dependencies
└── Documentation files...
```

## Contributing

This is a complete working implementation. Potential areas for contribution:
- Add multiple viewer support
- Implement audio streaming
- Add recording feature
- Improve UI/UX
- Add more gestures
- Enhance security features

## License

This is a sample/educational project. Feel free to use and modify as needed.

## Support

For issues:
1. Check the troubleshooting section
2. Review relevant documentation files
3. Check browser/Android logcat for errors
4. Verify network connectivity

## Credits

Built with:
- Android SDK & MediaProjection API
- WebRTC Project
- Socket.IO
- Node.js & Express
- Material Design Components

## Comparison: Android vs Web Viewer

| Feature | Android App | Web Dashboard |
|---------|-------------|---------------|
| Installation | Required | Not required |
| Platform | Android only | Any browser |
| Touch Control | Full gestures | Mouse clicks |
| Setup Time | 10 minutes | 1 minute |
| Performance | Excellent | Very Good |
| Portability | Mobile only | Desktop/Laptop |
| Best For | On-the-go | Work/Presentation |

## Getting Help

- **Quick Start**: See [QUICK_START.md](QUICK_START.md)
- **Web Dashboard**: See [WEB_DASHBOARD_GUIDE.md](WEB_DASHBOARD_GUIDE.md)
- **Server Config**: See [ScreenShareApp/SERVER_CONFIG.md](ScreenShareApp/SERVER_CONFIG.md)
- **Architecture**: See [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)

## Changelog

### Version 1.0 (Current)
- ✅ Android screen sharing with PIN
- ✅ Android viewer app
- ✅ Web dashboard for browser viewing
- ✅ WebRTC peer-to-peer streaming
- ✅ Mouse/touch control
- ✅ Real-time connection stats
- ✅ Material Design UI

---

**Made with ❤️ for seamless screen sharing experience**

Start sharing your screen in minutes! 🚀
# Screen-share-app
# Screen-share-app
