# Peer-to-Peer (P2P) File Sharing System

A simple, beginner-friendly P2P file sharing system implemented in pure Java. This system allows multiple peers to connect to each other and share files directly without a central server.

## Features

- **Pure Java Implementation**: No external dependencies required
- **TCP Socket Communication**: Reliable file transfer using TCP
- **Multi-threaded Server**: Handle multiple peer connections simultaneously  
- **CLI Interface**: Easy-to-use command-line menu system
- **File Integrity**: Preserves file content during transfer
- **Progress Feedback**: Real-time transfer progress updates
- **Cross-platform**: Works on Windows, Linux, and macOS

## Project Structure

```
java/
├── PeerClean.java           # Main class with CLI interface
├── ServerThreadClean.java   # Handles incoming connections
├── ClientHandlerClean.java  # Manages outgoing connections
├── FileManagerClean.java    # File operations utility class
├── shared/                  # Directory for files to share
│   ├── welcome.txt
│   ├── test-data.csv
│   └── java-tips.txt
├── downloads/               # Downloaded files are saved here
└── README.md               # This file
```

## Quick Start Guide

### 1. Compile the Code

```bash
javac *Clean.java
```

### 2. Start First Peer (Terminal 1)

```bash
java PeerClean 5000
```

### 3. Start Second Peer (Terminal 2)

```bash
java PeerClean 5001
```

### 4. Connect Peers

In Terminal 2 (Peer on port 5001):
1. Choose option `1` (Connect to peer)
2. Enter IP: `127.0.0.1`
3. Enter Port: `5000`

### 5. Share Files

1. Choose option `2` (List peer files) to see available files
2. Choose option `3` (Download file) to download a specific file
3. Enter the exact file name to download

## Menu Options

When you run a peer, you'll see this menu:

```
==================================================
[P2P] P2P FILE SHARING - MAIN MENU
==================================================
[STATUS] Status: Not connected to any peer

Choose an option:
0. [FILES] Create sample files for testing
1. [CONNECT] Connect to peer
2. [LIST] List peer files
3. [DOWNLOAD] Download file
4. [FOLDER] Show my shared files
5. [TEST] Test peer connectivity
6. [CLOSE] Disconnect from current peer
7. [EXIT] Exit
```

## Detailed Usage Instructions

### Adding Files to Share

1. Place files in the `shared/` directory
2. These files will automatically be available to other peers
3. Use option `4` to see your current shared files

### Connecting to Another Peer

1. Make sure the target peer is running
2. Use option `1` to connect
3. Enter the peer's IP address (127.0.0.1 for localhost)
4. Enter the peer's port number

### Testing Connectivity

Use option `5` to test if a peer is reachable before attempting to connect.

### Downloading Files

1. First connect to a peer (option `1`)
2. List available files (option `2`)
3. Download a file by exact name (option `3`)
4. Files are saved to the `downloads/` directory

## Testing Scenarios

### Scenario 1: Local Testing with Two Peers

1. **Terminal 1**: `java PeerClean 5000`
2. **Terminal 2**: `java PeerClean 5001`
3. In Terminal 2, connect to 127.0.0.1:5000
4. List files and download

### Scenario 2: Network Testing

1. **Computer A**: `java PeerClean 5000`
2. **Computer B**: `java PeerClean 5001`
3. In Computer B, connect to Computer A's IP address and port 5000

### Scenario 3: Multiple Peers

You can run multiple peers on different ports:
- `java PeerClean 5000`
- `java PeerClean 5001`
- `java PeerClean 5002`

Each peer can connect to any other peer.

## Technical Details

### Network Communication

- **Protocol**: TCP (Transmission Control Protocol)
- **Default Timeout**: 5 seconds for connections
- **Buffer Size**: 4KB for file transfers
- **Port Range**: 1024-65535 (user-defined)

### File Transfer Process

1. Client sends `LIST_FILES` or `DOWNLOAD_FILE` command
2. Server responds with file list or file data
3. File size is sent first, followed by file content
4. Progress is displayed during large file transfers

### Threading Model

- **Main Thread**: Handles CLI interface and user input
- **Server Thread**: Listens for incoming peer connections
- **Connection Handler Threads**: Handle individual peer requests
- **Client Thread**: Manages outgoing connections (implicit)

### Error Handling

- Connection timeouts
- File not found errors
- Network interruptions
- Invalid user input
- Port conflicts

## Troubleshooting

### Common Issues

1. **"Connection refused"**
   - Make sure the target peer is running
   - Check IP address and port number
   - Verify firewall settings

2. **"Port already in use"**
   - Choose a different port number
   - Kill existing processes using the port

3. **"File not found"**
   - Check exact file name spelling
   - Ensure file exists in shared/ directory

4. **"Permission denied"**
   - Check file/directory permissions
   - Run with appropriate user privileges

### Debugging Tips

- Use option `5` to test connectivity before connecting
- Check console output for detailed error messages
- Verify files exist in the `shared/` directory
- Use `netstat` command to check port usage

## Extending the System

### Possible Enhancements

1. **GUI Interface**: Add a graphical user interface
2. **File Search**: Search for files by name/content
3. **Peer Discovery**: Automatic peer discovery on local network
4. **Authentication**: Add user authentication and access control
5. **Encryption**: Encrypt file transfers for security
6. **Resume Downloads**: Support for resuming interrupted downloads
7. **Multiple Downloads**: Download multiple files simultaneously

### Code Modifications

The modular design makes it easy to extend:
- Modify `PeerClean.java` for interface changes
- Update `FileManagerClean.java` for new file operations
- Extend `ServerThreadClean.java` for new server features
- Enhance `ClientHandlerClean.java` for additional client functionality

## Learning Objectives

This project demonstrates:
- **Network Programming**: TCP sockets, client-server communication
- **Threading**: Multi-threaded programming, thread safety
- **File I/O**: Reading/writing files, buffered streams
- **Exception Handling**: Try-catch blocks, error recovery
- **Object-Oriented Design**: Classes, encapsulation, modularity

## License

This is an educational project. Feel free to use, modify, and distribute for learning purposes.

## Support

If you encounter issues:
1. Check the troubleshooting section
2. Verify your Java installation
3. Ensure proper network connectivity
4. Review the console output for error details

Happy file sharing! 🚀 