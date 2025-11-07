# Server Configuration Guide

## Important: Update Server URL Before Running

The app needs to know where your signaling server is running. You MUST update the server URL before using the app.

## File to Edit

Open: `app/src/main/java/com/example/screenshare/SignalingClient.java`

Find line ~14:
```java
private static final String SERVER_URL = "http://10.0.2.2:3000";
```

## Configuration Options

### Option 1: Testing with Android Emulator
```java
private static final String SERVER_URL = "http://10.0.2.2:3000";
```
- Use this when running the app in an Android emulator
- `10.0.2.2` is the emulator's alias for your computer's localhost

### Option 2: Testing with Real Android Device (Same Network)
```java
private static final String SERVER_URL = "http://192.168.1.XXX:3000";
```
- Replace `XXX` with your computer's actual IP address
- Both your phone and computer must be on the same Wi-Fi network

**How to find your IP:**
- **Windows**: Open Command Prompt → type `ipconfig` → look for "IPv4 Address"
- **macOS**: Open Terminal → type `ifconfig` → look for "inet" under your network interface
- **Linux**: Open Terminal → type `ip addr` or `ifconfig` → look for "inet"

### Option 3: Production Server
```java
private static final String SERVER_URL = "https://your-domain.com";
```
- Use this when you deploy the signaling server to a cloud service
- Make sure to use HTTPS in production

## Port Configuration

The default port is `3000`. If you changed the server port:

```java
private static final String SERVER_URL = "http://YOUR_IP:YOUR_PORT";
```

## Common Scenarios

### Scenario 1: Testing with 2 Emulators
- Both emulators use: `http://10.0.2.2:3000`

### Scenario 2: Testing with 1 Emulator + 1 Real Device
- Emulator uses: `http://10.0.2.2:3000`
- Real device uses: `http://YOUR_COMPUTER_IP:3000`

### Scenario 3: Testing with 2 Real Devices
- Both devices use: `http://YOUR_COMPUTER_IP:3000`
- All devices must be on the same network

## Verification

After updating the URL:
1. Rebuild the app in Android Studio
2. Or run: `./gradlew clean assembleDebug`
3. Reinstall the app on your device
4. Test the connection

## Troubleshooting

### Connection Refused
- Check that the signaling server is running (`npm start`)
- Verify the IP address is correct
- Check firewall settings

### Timeout
- Ensure devices are on the same network
- Verify no VPN is interfering
- Check if port 3000 is open

### SSL/Certificate Errors (Production)
- Use `https://` instead of `http://` for production
- Ensure your SSL certificate is valid
- Update Socket.IO client options if needed

## Example IPs by Network Type

| Network Type | Typical IP Range | Example |
|-------------|-----------------|---------|
| Home Wi-Fi | 192.168.0.X or 192.168.1.X | 192.168.1.100 |
| Office Network | 10.0.0.X or 172.16.0.X | 10.0.0.50 |
| Mobile Hotspot | 192.168.43.X | 192.168.43.1 |
| Emulator | 10.0.2.2 | 10.0.2.2 |

## After Changing Configuration

Remember to:
1. Save the file
2. Rebuild the project
3. Reinstall the app on all test devices
4. Restart the signaling server if you changed its configuration
