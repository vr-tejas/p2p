import java.io.*;
import java.net.*;
import java.util.List;

/**
 * ServerThreadClean - Runs as a separate thread to listen for incoming connections
 * 
 * This thread:
 * 1. Creates a ServerSocket on the specified port
 * 2. Continuously listens for incoming connections from other peers
 * 3. For each connection, handles the peer's requests (file list or file download)
 * 4. Supports multiple simultaneous connections using threads
 */
public class ServerThreadClean extends Thread {
    
    private int port;                // Port number to listen on
    private boolean running;         // Flag to control the server thread
    private ServerSocket serverSocket;  // Server socket for accepting connections
    
    /**
     * Constructor for ServerThread
     * @param port Port number to listen for incoming connections
     */
    public ServerThreadClean(int port) {
        this.port = port;
        this.running = true;
    }
    
    /**
     * Main execution method for the server thread
     * This method runs continuously until stopServer() is called
     */
    @Override
    public void run() {
        try {
            // Create server socket to listen for connections
            serverSocket = new ServerSocket(port);
            System.out.println("[OK] Server started on port " + port);
            System.out.println("[NETWORK] Waiting for peer connections...");
            
            // Keep listening for connections until stopped
            while (running) {
                try {
                    // Accept incoming connection from a peer
                    Socket clientSocket = serverSocket.accept();
                    
                    // Get peer's address for logging
                    String peerAddress = clientSocket.getInetAddress().getHostAddress();
                    int peerPort = clientSocket.getPort();
                    System.out.println("[CONNECT] New connection from peer: " + peerAddress + ":" + peerPort);
                    
                    // Handle this connection in a separate thread
                    // This allows multiple peers to connect simultaneously
                    Thread connectionHandler = new Thread(new ConnectionHandler(clientSocket));
                    connectionHandler.start();
                    
                } catch (IOException e) {
                    // If server is stopping, this exception is expected
                    if (running) {
                        System.err.println("Error accepting connection: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            if (e.getMessage().contains("Address already in use") || e.getMessage().contains("NET_Bind")) {
                System.err.println("[ERROR] Port " + port + " is already in use!");
                System.err.println("[TIP] Try a different port number or stop the process using this port");
                System.err.println("[TIP] Use 'netstat -ano | findstr :" + port + "' to find the process");
            } else {
                System.err.println("[ERROR] Could not start server on port " + port + ": " + e.getMessage());
            }
        } finally {
            // Clean up server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Stops the server thread gracefully
     */
    public void stopServer() {
        running = false;
        
        // Close server socket to interrupt accept() call
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("[STOP] Server stopped");
            } catch (IOException e) {
                System.err.println("Error stopping server: " + e.getMessage());
            }
        }
    }
    
    /**
     * ConnectionHandler - Inner class to handle individual peer connections
     * This runs in a separate thread for each connected peer
     */
    private static class ConnectionHandler implements Runnable {
        
        private Socket clientSocket;
        
        /**
         * Constructor for ConnectionHandler
         * @param clientSocket Socket connected to the peer
         */
        public ConnectionHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
        
        /**
         * Handles the communication with a connected peer
         */
        @Override
        public void run() {
            try {
                // Set up input and output streams for communication
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                
                String peerAddress = clientSocket.getInetAddress().getHostAddress();
                System.out.println("[INFO] Handling request from: " + peerAddress);
                
                // Read the command from the peer
                String command = in.readLine();
                
                if (command == null) {
                    System.out.println("[WARNING] No command received from peer");
                    return;
                }
                
                System.out.println("[MSG] Received command: " + command + " from " + peerAddress);
                
                // Handle different types of requests
                switch (command.toUpperCase()) {
                    case "LIST_FILES":
                        // Peer wants to see our available files
                        handleFileListRequest();
                        break;
                        
                    case "DOWNLOAD_FILE":
                        // Peer wants to download a specific file
                        handleFileDownloadRequest(in);
                        break;
                        
                    default:
                        // Unknown command
                        out.println("ERROR: Unknown command: " + command);
                        System.out.println("[ERROR] Unknown command received: " + command);
                        break;
                }
                
            } catch (IOException e) {
                System.err.println("Error handling peer connection: " + e.getMessage());
            } finally {
                // Always close the connection when done
                try {
                    String peerAddress = clientSocket.getInetAddress().getHostAddress();
                    clientSocket.close();
                    System.out.println("[CLOSE] Connection closed with peer: " + peerAddress);
                } catch (IOException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
        
        /**
         * Handles a file list request from a peer
         * Sends the list of available files in our shared directory
         */
        private void handleFileListRequest() {
            try {
                System.out.println("[LIST] Sending file list to peer...");
                
                // Use FileManagerClean to send our file list
                FileManagerClean.sendFileList(clientSocket);
                
            } catch (Exception e) {
                System.err.println("Error sending file list: " + e.getMessage());
            }
        }
        
        /**
         * Handles a file download request from a peer
         * @param in BufferedReader to read the file name from peer
         */
        private void handleFileDownloadRequest(BufferedReader in) {
            try {
                // Read the name of the file the peer wants
                String fileName = in.readLine();
                
                if (fileName == null || fileName.trim().isEmpty()) {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("ERROR: No file name provided");
                    System.out.println("[ERROR] No file name provided for download");
                    return;
                }
                
                fileName = fileName.trim();
                System.out.println("[SEND] Peer requested file: " + fileName);
                
                // Check if the file exists in our shared directory
                List<String> availableFiles = FileManagerClean.getAvailableFiles();
                if (!availableFiles.contains(fileName)) {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("ERROR: File not found: " + fileName);
                    System.out.println("[ERROR] Requested file not found: " + fileName);
                    return;
                }
                
                // Send confirmation that file exists and will be sent
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("OK");
                
                // Use FileManagerClean to send the file
                boolean success = FileManagerClean.sendFile(fileName, clientSocket);
                
                if (success) {
                    System.out.println("[OK] File sent successfully: " + fileName);
                } else {
                    System.out.println("[ERROR] Failed to send file: " + fileName);
                }
                
            } catch (IOException e) {
                System.err.println("Error handling file download: " + e.getMessage());
            }
        }
    }
} 