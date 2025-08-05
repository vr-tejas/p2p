# P2P File Sharing System

A peer-to-peer file sharing application built in Java. Each peer can connect to others and share files directly without needing a central server.

## How it works

Each peer runs both a server (to share files) and a client (to download files). Peers connect to each other using IP addresses and ports, then can browse and download each other's files.

## Quick Start

### Compile
```bash
javac *.java
```

### Run two peers
Terminal 1:
```bash
java Peer 5000
```

Terminal 2:
```bash
java Peer 5001
```

### Connect and share
1. In Terminal 2, choose option 1 (Connect to peer)
2. Enter IP: `127.0.0.1` and Port: `5000`
3. Use option 2 to list files from the other peer
4. Use option 3 to download files

## File structure

```
├── Peer.java          # Main application with menu interface
├── ServerThread.java   # Handles incoming connections from other peers
├── ClientHandler.java  # Manages connections to other peers
├── FileManager.java    # File operations (sending/receiving files)
├── shared/            # Put files here to share with others
└── downloads/         # Downloaded files are saved here
```

## Features

- Multi-threaded server handles multiple connections
- Real-time file transfer with progress tracking
- Simple command-line interface
- Works on any platform with Java
- No external dependencies

## Menu options

- **0** - Create sample files for testing
- **1** - Connect to another peer
- **2** - List files available on connected peer
- **3** - Download a file from connected peer
- **4** - Show your own shared files
- **5** - Test if a peer is reachable
- **6** - Disconnect from current peer
- **7** - Exit

## Technical details

- Uses TCP sockets for reliable file transfer
- Each connection runs in its own thread
- Files are transferred in 4KB chunks
- Supports any file type (text, binary, images, etc.)
- Graceful error handling for network issues

## Testing locally

You can test with multiple peers on the same machine using different ports (5000, 5001, 5002, etc.) and connecting to `127.0.0.1`.

For testing on different machines, use the actual IP addresses instead of localhost.