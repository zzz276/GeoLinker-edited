import express from 'express';
import { Server } from 'socket.io';
import { WebSocketServer } from 'ws';

const app = express();
const httpServer = app.listen(3000, '0.0.0.0', () => { console.log('Socket.IO server running on port 3000'); });

// WebSocket server for ESP32
const wss = new WebSocketServer({ port: 8080 }, () => { console.log('WebSocket server running on port 8080'); });

// Handle ESP32 connections
wss.on('connection', ws => {
  console.log('ESP32 connected');

  ws.on('message', message => {
    try {
      const data = JSON.parse(message);
      console.log('Received GPS:', data);

      // Broadcast to all Socket.IO clients
      io.emit('location', data);
    } catch (err) { console.error('Invalid JSON:', err); }
  });

  ws.on('close', () => { console.log('ESP32 disconnected'); });
});

// Socket.IO server for mobile clients
const io = new Server(httpServer, { cors: { origin: '*' }});

// Handle mobile client connections
io.on('connection', socket => {
  console.log('Mobile client connected');

  socket.on('disconnect', () => { console.log('Mobile client disconnected'); });
});
