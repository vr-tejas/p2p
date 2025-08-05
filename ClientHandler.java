import java.io.*;
import java.net.*;
import java.util.List;

/**
 * ClientHandlerClean - Handles outgoing connections to other peers
 * 
 * This class provides methods to:
 * 1. Connect to another peer using IP address and port
 * 2. Request and receive a list of files from the peer
 * 3. Download specific files from the peer
 * 4. Handle all client-side network communication
 */
public class ClientHandler {
    
    private String peerIP;      // IP address of the peer to connect to
    private int peerPort;       // Port number of the peer
    private Socket socket;      // Socket for connection to peer
    private boolean connected;  // Connection status
    
    /**
     * Constructor for ClientHandler
     * @param peerIP IP address of the peer to connect to
     * @param peerPort Port number of the peer
     */
    public ClientHandler(String peerIP, int peerPort) {
        this.peerIP = peerIP;
        this.peerPort = peerPort;
        this.connected = false;
    }
    
    /**
     * Attempts to connect to the specified peer
     * @return true if connection successful, false otherwise
     */
    public boolean connectToPeer() {
        try {
            System.out.println("[CONNECT] Attempting to connect to peer: " + peerIP + ":" + peerPort);
            
            // Create socket connection to peer
            // Timeout after 5 seconds if peer doesn't respond
            socket = new Socket();
            socket.connect(new InetSocketAddress(peerIP, peerPort), 5000);
            
            connected = true;
            System.out.println("[OK] Successfully connected to peer: " + peerIP + ":" + peerPort);
            return true;
            
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to connect to peer " + peerIP + ":" + peerPort);
            System.err.println("   Reason: " + e.getMessage());
            connected = false;
            return false;
        }
    }
    
    /**
     * Disconnects from the current peer
     */
    public void disconnect() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                System.out.println("[CLOSE] Disconnected from peer: " + peerIP + ":" + peerPort);
            } catch (IOException e) {
                System.err.println("Error disconnecting: " + e.getMessage());
            }
        }
        connected = false;
    }
    
    /**
     * Requests a list of available files from the connected peer
     * @return List of file names available on the peer, or null if error occurred
     */
    public List<String> requestFileList() {
        if (!connected || socket == null || socket.isClosed()) {
            System.err.println("[ERROR] Not connected to any peer");
            return null;
        }
        
        try {
            System.out.println("[LIST] Requesting file list from peer...");
            
            // Send LIST_FILES command to peer
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("LIST_FILES");
            
                            // Receive file list using FileManager
                List<String> files = FileManager.receiveFileList(socket);
            
            System.out.println("[INFO] Received " + files.size() + " files from peer");
            return files;
            
        } catch (IOException e) {
            System.err.println("[ERROR] Error requesting file list: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Downloads a specific file from the connected peer
     * @param fileName Name of the file to download
     * @return true if download successful, false otherwise
     */
    public boolean downloadFile(String fileName) {
        if (!connected || socket == null || socket.isClosed()) {
            System.err.println("[ERROR] Not connected to any peer");
            return false;
        }
        
        if (fileName == null || fileName.trim().isEmpty()) {
            System.err.println("[ERROR] Invalid file name");
            return false;
        }
        
        try {
            System.out.println("[DOWNLOAD] Requesting download: " + fileName);
            
            // Send DOWNLOAD_FILE command to peer
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("DOWNLOAD_FILE");
            out.println(fileName.trim());
            
            // Read response from peer
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            
            if (response == null) {
                System.err.println("[ERROR] No response from peer");
                return false;
            }
            
            if (response.startsWith("ERROR")) {
                System.err.println("[ERROR] Peer error: " + response);
                return false;
            }
            
            if (!response.equals("OK")) {
                System.err.println("[ERROR] Unexpected response from peer: " + response);
                return false;
            }
            
            // Peer confirmed file exists, now receive it
            System.out.println("[NETWORK] Peer confirmed file availability, starting download...");
            boolean success = FileManager.receiveFile(fileName, socket);
            
            if (success) {
                System.out.println("[SUCCESS] File downloaded successfully: " + fileName);
            } else {
                System.out.println("[ERROR] Failed to download file: " + fileName);
            }
            
            return success;
            
        } catch (IOException e) {
            System.err.println("[ERROR] Error downloading file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if currently connected to a peer
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
    
    /**
     * Gets the IP address of the connected peer
     * @return IP address string, or null if not connected
     */
    public String getPeerIP() {
        return peerIP;
    }
    
    /**
     * Gets the port number of the connected peer
     * @return Port number, or -1 if not connected
     */
    public int getPeerPort() {
        return peerPort;
    }
    
    /**
     * Static method to test if a peer is reachable
     * This is useful before attempting to connect
     * @param ip IP address to test
     * @param port Port number to test
     * @return true if peer is reachable, false otherwise
     */
    public static boolean testPeerReachability(String ip, int port) {
        try {
            System.out.println("[TEST] Testing connectivity to " + ip + ":" + port + "...");
            
            Socket testSocket = new Socket();
            testSocket.connect(new InetSocketAddress(ip, port), 3000); // 3 second timeout
            testSocket.close();
            
            System.out.println("[OK] Peer is reachable: " + ip + ":" + port);
            return true;
            
        } catch (IOException e) {
            System.out.println("[ERROR] Peer is not reachable: " + ip + ":" + port);
            return false;
        }
    }
    
    /**
     * Creates a new connection to a different peer
     * Automatically disconnects from current peer if connected
     * @param newPeerIP IP address of new peer
     * @param newPeerPort Port of new peer
     * @return true if connection to new peer successful
     */
    public boolean switchToPeer(String newPeerIP, int newPeerPort) {
        // Disconnect from current peer if connected
        if (isConnected()) {
            System.out.println("[SWITCH] Switching from current peer...");
            disconnect();
        }
        
        // Update peer information
        this.peerIP = newPeerIP;
        this.peerPort = newPeerPort;
        
        // Connect to new peer
        return connectToPeer();
    }
    
    /**
     * Gets information about the current connection
     * @return String describing the connection status
     */
    public String getConnectionInfo() {
        if (isConnected()) {
            return "Connected to peer: " + peerIP + ":" + peerPort;
        } else {
            return "Not connected to any peer";
        }
    }
} 