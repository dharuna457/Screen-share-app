const express = require('express');
const path = require('path');
const app = express();
const http = require('http').createServer(app);
const io = require('socket.io')(http, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});

const PORT = process.env.PORT || 3000;

// Store active sessions: PIN -> { hostSocketId, viewerSocketId, hostSocket, viewerSocket }
const sessions = new Map();

// Serve static files from 'public' directory
app.use(express.static(path.join(__dirname, 'public')));

app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.get('/status', (req, res) => {
    res.json({
        activeSessions: sessions.size,
        sessions: Array.from(sessions.keys())
    });
});

io.on('connection', (socket) => {
    console.log(`Client connected: ${socket.id}`);

    // Host creates a session with a PIN
    socket.on('create-session', (data) => {
        const { pin } = data;
        console.log(`Creating session with PIN: ${pin}`);

        if (sessions.has(pin)) {
            socket.emit('error', { message: 'PIN already in use' });
            return;
        }

        sessions.set(pin, {
            hostSocketId: socket.id,
            hostSocket: socket,
            viewerSocketId: null,
            viewerSocket: null
        });

        socket.pin = pin;
        socket.role = 'host';

        socket.emit('session-created', { pin });
        console.log(`Session created: ${pin} by ${socket.id}`);
    });

    // Viewer joins a session with a PIN
    socket.on('join-session', (data) => {
        const { pin } = data;
        console.log(`Viewer attempting to join session: ${pin}`);

        const session = sessions.get(pin);

        if (!session) {
            socket.emit('error', { message: 'Invalid PIN' });
            return;
        }

        if (session.viewerSocketId) {
            socket.emit('error', { message: 'Session already has a viewer' });
            return;
        }

        session.viewerSocketId = socket.id;
        session.viewerSocket = socket;
        socket.pin = pin;
        socket.role = 'viewer';

        // Notify both parties
        socket.emit('session-joined', { pin });
        session.hostSocket.emit('viewer-joined', { viewerId: socket.id });

        console.log(`Viewer ${socket.id} joined session ${pin}`);
    });

    // Forward WebRTC offer from host to viewer
    socket.on('offer', (data) => {
        const { pin, offer } = data;
        const session = sessions.get(pin);

        if (!session || !session.viewerSocket) {
            socket.emit('error', { message: 'No viewer connected' });
            return;
        }

        console.log(`Forwarding offer from ${socket.id} to viewer`);
        session.viewerSocket.emit('offer', { offer });
    });

    // Forward WebRTC answer from viewer to host
    socket.on('answer', (data) => {
        const { pin, answer } = data;
        const session = sessions.get(pin);

        if (!session || !session.hostSocket) {
            socket.emit('error', { message: 'Host not found' });
            return;
        }

        console.log(`Forwarding answer from ${socket.id} to host`);
        session.hostSocket.emit('answer', { answer });
    });

    // Forward ICE candidates
    socket.on('ice-candidate', (data) => {
        const { pin, candidate } = data;
        const session = sessions.get(pin);

        if (!session) {
            return;
        }

        // Forward to the other peer
        if (socket.role === 'host' && session.viewerSocket) {
            session.viewerSocket.emit('ice-candidate', { candidate });
        } else if (socket.role === 'viewer' && session.hostSocket) {
            session.hostSocket.emit('ice-candidate', { candidate });
        }
    });

    // Handle touch events from viewer
    socket.on('touch-event', (data) => {
        const { pin, x, y, action } = data;
        const session = sessions.get(pin);

        if (session && session.hostSocket) {
            session.hostSocket.emit('touch-event', { x, y, action });
        }
    });

    // Handle disconnection
    socket.on('disconnect', () => {
        console.log(`Client disconnected: ${socket.id}`);

        if (socket.pin) {
            const session = sessions.get(socket.pin);

            if (session) {
                // Notify the other peer
                if (socket.role === 'host') {
                    if (session.viewerSocket) {
                        session.viewerSocket.emit('host-disconnected');
                    }
                    sessions.delete(socket.pin);
                    console.log(`Session ${socket.pin} deleted (host disconnected)`);
                } else if (socket.role === 'viewer') {
                    session.viewerSocketId = null;
                    session.viewerSocket = null;
                    if (session.hostSocket) {
                        session.hostSocket.emit('viewer-disconnected');
                    }
                    console.log(`Viewer left session ${socket.pin}`);
                }
            }
        }
    });

    socket.on('end-session', () => {
        if (socket.pin) {
            const session = sessions.get(socket.pin);
            if (session) {
                if (session.viewerSocket) {
                    session.viewerSocket.emit('session-ended');
                }
                if (session.hostSocket) {
                    session.hostSocket.emit('session-ended');
                }
                sessions.delete(socket.pin);
                console.log(`Session ${socket.pin} ended by user`);
            }
        }
    });
});

http.listen(PORT, '0.0.0.0', () => {
    console.log(`Signaling server running on port ${PORT}`);
    console.log(`Access at: http://localhost:${PORT}`);
});
