// Screen Share Web Viewer Application

class ScreenShareViewer {
    constructor() {
        // DOM Elements
        this.pinSection = document.getElementById('pinSection');
        this.videoSection = document.getElementById('videoSection');
        this.pinInput = document.getElementById('pinInput');
        this.connectBtn = document.getElementById('connectBtn');
        this.disconnectBtn = document.getElementById('disconnectBtn');
        this.statusMessage = document.getElementById('statusMessage');
        this.remoteVideo = document.getElementById('remoteVideo');
        this.videoOverlay = document.getElementById('videoOverlay');
        this.currentPinDisplay = document.getElementById('currentPin');
        this.connectionStatus = document.getElementById('connectionStatus');
        this.enableMouseControl = document.getElementById('enableMouseControl');
        this.statsPanel = document.getElementById('statsPanel');
        this.iceState = document.getElementById('iceState');
        this.signalingState = document.getElementById('signalingState');
        this.videoResolution = document.getElementById('videoResolution');

        // State
        this.socket = null;
        this.peerConnection = null;
        this.currentPin = null;
        this.isConnected = false;

        // WebRTC Configuration
        this.rtcConfig = {
            iceServers: [
                { urls: 'stun:stun.l.google.com:19302' },
                { urls: 'stun:stun1.l.google.com:19302' }
            ]
        };

        this.init();
    }

    init() {
        // Event Listeners
        this.connectBtn.addEventListener('click', () => this.connect());
        this.disconnectBtn.addEventListener('click', () => this.disconnect());
        this.pinInput.addEventListener('input', (e) => this.formatPinInput(e));
        this.pinInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && this.pinInput.value.length === 6) {
                this.connect();
            }
        });

        // Mouse control
        this.remoteVideo.addEventListener('click', (e) => this.handleRemoteClick(e));
        this.remoteVideo.addEventListener('mousedown', (e) => this.handleRemoteMouseDown(e));
        this.remoteVideo.addEventListener('mouseup', (e) => this.handleRemoteMouseUp(e));
        this.remoteVideo.addEventListener('mousemove', (e) => this.handleRemoteMouseMove(e));

        // Video loaded event
        this.remoteVideo.addEventListener('loadedmetadata', () => {
            this.updateVideoResolution();
        });

        console.log('Screen Share Viewer initialized');
    }

    formatPinInput(e) {
        // Only allow numbers
        e.target.value = e.target.value.replace(/[^0-9]/g, '');
    }

    showStatus(message, type = 'info') {
        this.statusMessage.textContent = message;
        this.statusMessage.className = `status-message show ${type}`;
        setTimeout(() => {
            this.statusMessage.classList.remove('show');
        }, 5000);
    }

    async connect() {
        const pin = this.pinInput.value.trim();

        if (pin.length !== 6) {
            this.showStatus('Please enter a valid 6-digit PIN', 'error');
            return;
        }

        this.currentPin = pin;
        this.connectBtn.disabled = true;
        this.showStatus('Connecting...', 'info');

        try {
            // Initialize Socket.IO
            this.initSocket();

            // Initialize WebRTC
            this.initPeerConnection();

            // Join session
            this.socket.emit('join-session', { pin });

        } catch (error) {
            console.error('Connection error:', error);
            this.showStatus('Connection failed: ' + error.message, 'error');
            this.connectBtn.disabled = false;
        }
    }

    initSocket() {
        // Connect to signaling server (same origin)
        this.socket = io();

        this.socket.on('connect', () => {
            console.log('Socket connected:', this.socket.id);
        });

        this.socket.on('session-joined', (data) => {
            console.log('Session joined:', data.pin);
            this.showStatus('Joined session. Waiting for host...', 'success');
        });

        this.socket.on('offer', async (data) => {
            console.log('Received offer');
            await this.handleOffer(data.offer);
        });

        this.socket.on('ice-candidate', async (data) => {
            console.log('Received ICE candidate');
            await this.handleIceCandidate(data.candidate);
        });

        this.socket.on('error', (data) => {
            console.error('Server error:', data.message);
            this.showStatus(data.message, 'error');
            this.connectBtn.disabled = false;
        });

        this.socket.on('host-disconnected', () => {
            console.log('Host disconnected');
            this.showStatus('Host disconnected', 'error');
            setTimeout(() => this.disconnect(), 2000);
        });

        this.socket.on('session-ended', () => {
            console.log('Session ended by host');
            this.showStatus('Session ended by host', 'error');
            setTimeout(() => this.disconnect(), 2000);
        });

        this.socket.on('disconnect', () => {
            console.log('Socket disconnected');
            if (this.isConnected) {
                this.showStatus('Connection lost', 'error');
            }
        });
    }

    initPeerConnection() {
        this.peerConnection = new RTCPeerConnection(this.rtcConfig);

        // Handle incoming streams
        this.peerConnection.ontrack = (event) => {
            console.log('Received remote track:', event.streams[0]);
            this.remoteVideo.srcObject = event.streams[0];
            this.videoOverlay.classList.add('hidden');
            this.isConnected = true;

            // Switch to video view
            this.pinSection.style.display = 'none';
            this.videoSection.style.display = 'block';
            this.statsPanel.style.display = 'block';
            this.currentPinDisplay.textContent = this.currentPin;
        };

        // Handle ICE candidates
        this.peerConnection.onicecandidate = (event) => {
            if (event.candidate) {
                console.log('Sending ICE candidate');
                this.socket.emit('ice-candidate', {
                    pin: this.currentPin,
                    candidate: {
                        sdpMid: event.candidate.sdpMid,
                        sdpMLineIndex: event.candidate.sdpMLineIndex,
                        sdp: event.candidate.candidate
                    }
                });
            }
        };

        // Connection state changes
        this.peerConnection.oniceconnectionstatechange = () => {
            console.log('ICE connection state:', this.peerConnection.iceConnectionState);
            this.updateConnectionStatus();
            this.iceState.textContent = this.peerConnection.iceConnectionState;
        };

        this.peerConnection.onsignalingstatechange = () => {
            console.log('Signaling state:', this.peerConnection.signalingState);
            this.signalingState.textContent = this.peerConnection.signalingState;
        };

        this.peerConnection.onconnectionstatechange = () => {
            console.log('Connection state:', this.peerConnection.connectionState);

            if (this.peerConnection.connectionState === 'disconnected' ||
                this.peerConnection.connectionState === 'failed') {
                this.showStatus('Connection lost', 'error');
            }
        };
    }

    async handleOffer(offer) {
        try {
            await this.peerConnection.setRemoteDescription(new RTCSessionDescription(offer));
            console.log('Remote description set');

            const answer = await this.peerConnection.createAnswer();
            await this.peerConnection.setLocalDescription(answer);
            console.log('Local description set');

            this.socket.emit('answer', {
                pin: this.currentPin,
                answer: {
                    type: answer.type,
                    sdp: answer.sdp
                }
            });
            console.log('Answer sent');

        } catch (error) {
            console.error('Error handling offer:', error);
            this.showStatus('Failed to establish connection', 'error');
        }
    }

    async handleIceCandidate(candidate) {
        try {
            await this.peerConnection.addIceCandidate(new RTCIceCandidate({
                sdpMid: candidate.sdpMid,
                sdpMLineIndex: candidate.sdpMLineIndex,
                candidate: candidate.sdp
            }));
            console.log('ICE candidate added');
        } catch (error) {
            console.error('Error adding ICE candidate:', error);
        }
    }

    updateConnectionStatus() {
        const state = this.peerConnection.iceConnectionState;
        const statusIndicator = document.querySelector('.status-indicator');

        switch (state) {
            case 'connected':
            case 'completed':
                this.connectionStatus.textContent = 'Connected';
                statusIndicator.classList.add('connected');
                this.remoteVideo.classList.add('control-enabled');
                break;
            case 'disconnected':
                this.connectionStatus.textContent = 'Disconnected';
                statusIndicator.classList.remove('connected');
                this.remoteVideo.classList.remove('control-enabled');
                break;
            case 'failed':
                this.connectionStatus.textContent = 'Failed';
                statusIndicator.classList.remove('connected');
                this.remoteVideo.classList.remove('control-enabled');
                break;
            case 'checking':
                this.connectionStatus.textContent = 'Connecting...';
                break;
        }
    }

    updateVideoResolution() {
        const width = this.remoteVideo.videoWidth;
        const height = this.remoteVideo.videoHeight;
        this.videoResolution.textContent = `${width}x${height}`;
    }

    // Mouse control methods
    handleRemoteClick(event) {
        if (!this.enableMouseControl.checked || !this.isConnected) return;

        const coords = this.getRelativeCoordinates(event);
        console.log('Click:', coords);

        // Send touch down
        this.sendTouchEvent(coords.x, coords.y, 'DOWN');

        // Send touch up after a short delay
        setTimeout(() => {
            this.sendTouchEvent(coords.x, coords.y, 'UP');
        }, 100);
    }

    handleRemoteMouseDown(event) {
        if (!this.enableMouseControl.checked || !this.isConnected) return;

        const coords = this.getRelativeCoordinates(event);
        this.sendTouchEvent(coords.x, coords.y, 'DOWN');
    }

    handleRemoteMouseUp(event) {
        if (!this.enableMouseControl.checked || !this.isConnected) return;

        const coords = this.getRelativeCoordinates(event);
        this.sendTouchEvent(coords.x, coords.y, 'UP');
    }

    handleRemoteMouseMove(event) {
        if (!this.enableMouseControl.checked || !this.isConnected) return;

        // Only send move events if mouse button is pressed
        if (event.buttons === 1) {
            const coords = this.getRelativeCoordinates(event);
            this.sendTouchEvent(coords.x, coords.y, 'MOVE');
        }
    }

    getRelativeCoordinates(event) {
        const rect = this.remoteVideo.getBoundingClientRect();
        const x = (event.clientX - rect.left) / rect.width;
        const y = (event.clientY - rect.top) / rect.height;

        return {
            x: Math.max(0, Math.min(1, x)),
            y: Math.max(0, Math.min(1, y))
        };
    }

    sendTouchEvent(x, y, action) {
        if (!this.socket || !this.currentPin) return;

        this.socket.emit('touch-event', {
            pin: this.currentPin,
            x: x,
            y: y,
            action: action
        });
    }

    disconnect() {
        console.log('Disconnecting...');

        if (this.socket) {
            this.socket.emit('end-session');
            this.socket.disconnect();
            this.socket = null;
        }

        if (this.peerConnection) {
            this.peerConnection.close();
            this.peerConnection = null;
        }

        if (this.remoteVideo.srcObject) {
            this.remoteVideo.srcObject.getTracks().forEach(track => track.stop());
            this.remoteVideo.srcObject = null;
        }

        // Reset UI
        this.pinSection.style.display = 'block';
        this.videoSection.style.display = 'none';
        this.statsPanel.style.display = 'none';
        this.videoOverlay.classList.remove('hidden');
        this.connectBtn.disabled = false;
        this.pinInput.value = '';
        this.currentPin = null;
        this.isConnected = false;

        this.showStatus('Disconnected', 'info');
    }
}

// Initialize the application when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    new ScreenShareViewer();
});
