import java.util.List;
import java.util.Scanner;

/**
 * =========================================
 * PEER-TO-PEER FILE SHARING SYSTEM
 * =========================================
 * 
 * HOW TO RUN AND TEST:
 * 
 * 1. COMPILE ALL FILES:
 *    javac *.java
 * 
 * 2. RUN FIRST PEER (Terminal 1):
 *    java PeerClean 5000
 *    (This starts a peer listening on port 5000)
 * 
 * 3. RUN SECOND PEER (Terminal 2):
 *    java PeerClean 5001
 *    (This starts a peer listening on port 5001)
 * 
 * 4. TESTING STEPS:
 *    - Both peers will start their servers automatically
 *    - Use the menu to connect peers to each other
 *    - Add some sample files to test with (option 0 in menu)
 *    - Connect to other peer using "1. Connect to peer"
 *    - List files using "2. List peer files"
 *    - Download files using "3. Download file"
 * 
 * 5. EXAMPLE TEST SCENARIO:
 *    Terminal 1 (Port 5000): Create sample files, start server
 *    Terminal 2 (Port 5001): Connect to 127.0.0.1:5000, list files, download
 * 
 * DIRECTORY STRUCTURE:
 * - shared/     <- Put files here to share with other peers
 * - downloads/  <- Downloaded files will be saved here
 * 
 * =========================================
 */
public class Peer {
    
    private int myPort;                    // Port this peer listens on
    private ServerThread serverThread;     // Thread handling incoming connections
    private ClientHandler clientHandler;   // Handler for outgoing connections
    private Scanner scanner;               // For reading user input
    private boolean running;               // Main program loop control
    
    /**
     * Constructor for Peer
     * @param port Port number for this peer to listen on
     */
    public Peer(int port) {
        this.myPort = port;
        this.scanner = new Scanner(System.in);
        this.running = true;
        
        // Start server thread to listen for incoming connections
        startServer();
    }
    
    /**
     * Starts the server thread to listen for incoming peer connections
     */
    private void startServer() {
        try {
            serverThread = new ServerThread(myPort);
            serverThread.start();
            
            // Give server a moment to start
            Thread.sleep(1000);
            
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to start server: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Main program loop - displays menu and handles user input
     */
    public void run() {
        System.out.println("\n[SUCCESS] Welcome to P2P File Sharing System!");
        System.out.println("[NETWORK] Your peer is running on port: " + myPort);
        System.out.println("[TIP] Tip: Files in 'shared/' folder will be available to other peers");
        
        // Create sample files for testing if shared directory is empty
        createInitialSampleFiles();
        
        while (running) {
            try {
                showMenu();
                int choice = getUserChoice();
                handleMenuChoice(choice);
                
                // Small pause for readability
                Thread.sleep(500);
                
            } catch (Exception e) {
                System.err.println("[ERROR] Error: " + e.getMessage());
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
            }
        }
        
        cleanup();
    }
    
    /**
     * Displays the main menu options
     */
    private void showMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("[P2P] P2P FILE SHARING - MAIN MENU");
        System.out.println("=".repeat(50));
        
        // Show current connection status
        if (clientHandler != null && clientHandler.isConnected()) {
            System.out.println("[STATUS] Status: " + clientHandler.getConnectionInfo());
        } else {
            System.out.println("[STATUS] Status: Not connected to any peer");
        }
        
        System.out.println("\nChoose an option:");
        System.out.println("0. [FILES] Create sample files for testing");
        System.out.println("1. [CONNECT] Connect to peer");
        System.out.println("2. [LIST] List peer files");
        System.out.println("3. [DOWNLOAD] Download file");
        System.out.println("4. [FOLDER] Show my shared files");
        System.out.println("5. [TEST] Test peer connectivity");
        System.out.println("6. [CLOSE] Disconnect from current peer");
        System.out.println("7. [EXIT] Exit");
        System.out.print("\nEnter your choice (0-7): ");
    }
    
    /**
     * Gets user input and validates it
     * @return User's menu choice
     */
    private int getUserChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("[WARNING] Please enter a valid number!");
            return -1; // Invalid choice
        }
    }
    
    /**
     * Handles the user's menu choice
     * @param choice Menu option selected by user
     */
    private void handleMenuChoice(int choice) {
        switch (choice) {
            case 0:
                createSampleFiles();
                break;
            case 1:
                connectToPeer();
                break;
            case 2:
                listPeerFiles();
                break;
            case 3:
                downloadFile();
                break;
            case 4:
                showMyFiles();
                break;
            case 5:
                testPeerConnectivity();
                break;
            case 6:
                disconnectFromPeer();
                break;
            case 7:
                exitProgram();
                break;
            default:
                System.out.println("[WARNING] Invalid choice! Please select 0-7.");
                break;
        }
    }
    
    /**
     * Creates sample files for testing purposes
     */
    private void createSampleFiles() {
        System.out.println("\n[FILES] Creating sample files for testing...");
        
        // Create some sample files with different content
        FileManager.createSampleFile("hello.txt", 
            "Hello from peer on port " + myPort + "!\n" +
            "This is a sample text file for testing the P2P system.\n" +
            "Current timestamp: " + System.currentTimeMillis());
            
        FileManager.createSampleFile("info.txt",
            "=== PEER INFORMATION ===\n" +
            "Peer Port: " + myPort + "\n" +
            "System: " + System.getProperty("os.name") + "\n" +
            "Java Version: " + System.getProperty("java.version") + "\n" +
            "Created: " + new java.util.Date());
            
        FileManager.createSampleFile("data.txt",
            "Sample data file with some numbers:\n" +
            "1, 2, 3, 4, 5, 6, 7, 8, 9, 10\n" +
            "Random number: " + (int)(Math.random() * 1000));
        
        System.out.println("[OK] Sample files created in 'shared/' directory");
        showMyFiles();
    }
    
    /**
     * Creates initial sample files if shared directory is empty
     */
    private void createInitialSampleFiles() {
        List<String> files = FileManager.getAvailableFiles();
        if (files.isEmpty()) {
            System.out.println("[FILES] Shared directory is empty. Creating initial sample files...");
            createSampleFiles();
        }
    }
    
    /**
     * Handles connecting to another peer
     */
    private void connectToPeer() {
        System.out.println("\n[CONNECT] Connect to Peer");
        System.out.println("Enter peer details to connect:");
        
        System.out.print("IP Address (e.g., 127.0.0.1): ");
        String ip = scanner.nextLine().trim();
        
        if (ip.isEmpty()) {
            ip = "127.0.0.1"; // Default to localhost
            System.out.println("Using default IP: " + ip);
        }
        
        System.out.print("Port (e.g., 5001): ");
        String portStr = scanner.nextLine().trim();
        
        try {
            int port = Integer.parseInt(portStr);
            
            // Don't allow connecting to ourselves
            if (port == myPort && (ip.equals("127.0.0.1") || ip.equals("localhost"))) {
                System.out.println("[ERROR] Cannot connect to yourself!");
                return;
            }
            
            // Disconnect from current peer if connected
            if (clientHandler != null && clientHandler.isConnected()) {
                clientHandler.disconnect();
            }
            
            // Create new client handler and connect
            clientHandler = new ClientHandler(ip, port);
            
            if (clientHandler.connectToPeer()) {
                System.out.println("[SUCCESS] Successfully connected! You can now list files and download.");
            } else {
                System.out.println("[TIP] Make sure the other peer is running and accessible.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid port number! Please enter a valid number.");
        }
    }
    
    /**
     * Lists files available on the connected peer
     */
    private void listPeerFiles() {
        if (clientHandler == null || !clientHandler.isConnected()) {
            System.out.println("[ERROR] You must connect to a peer first!");
            return;
        }
        
        System.out.println("\n[LIST] Requesting file list from peer...");
        
        List<String> files = clientHandler.requestFileList();
        
        if (files == null) {
            System.out.println("[ERROR] Failed to get file list from peer");
            return;
        }
        
        if (files.isEmpty()) {
            System.out.println("[FOLDER] Peer has no files available for sharing");
            return;
        }
        
        System.out.println("\n[FILES] Files available on peer:");
        System.out.println("-".repeat(40));
        for (int i = 0; i < files.size(); i++) {
            System.out.println((i + 1) + ". " + files.get(i));
        }
        System.out.println("-".repeat(40));
        System.out.println("Total files: " + files.size());
    }
    
    /**
     * Handles downloading a file from the connected peer
     */
    private void downloadFile() {
        if (clientHandler == null || !clientHandler.isConnected()) {
            System.out.println("[ERROR] You must connect to a peer first!");
            return;
        }
        
        System.out.println("\n[DOWNLOAD] Download File");
        System.out.print("Enter the exact file name to download: ");
        String fileName = scanner.nextLine().trim();
        
        if (fileName.isEmpty()) {
            System.out.println("[ERROR] File name cannot be empty!");
            return;
        }
        
        System.out.println("\n[START] Starting download...");
        boolean success = clientHandler.downloadFile(fileName);
        
        if (success) {
            System.out.println("[SUCCESS] Download completed successfully!");
            System.out.println("[FOLDER] Check the 'downloads/' folder for your file.");
        } else {
            System.out.println("[ERROR] Download failed. Please check the file name and try again.");
        }
    }
    
    /**
     * Shows files available in this peer's shared directory
     */
    private void showMyFiles() {
        System.out.println("\n[FOLDER] My Shared Files");
        
        List<String> files = FileManager.getAvailableFiles();
        
        if (files.isEmpty()) {
            System.out.println("[FILES] No files in shared directory");
            System.out.println("[TIP] Add files to the 'shared/' folder to share them with other peers");
            return;
        }
        
        System.out.println("-".repeat(40));
        for (int i = 0; i < files.size(); i++) {
            System.out.println((i + 1) + ". " + files.get(i));
        }
        System.out.println("-".repeat(40));
        System.out.println("Total files: " + files.size());
        System.out.println("[LOCATION] Location: shared/ directory");
    }
    
    /**
     * Tests connectivity to a peer without establishing a persistent connection
     */
    private void testPeerConnectivity() {
        System.out.println("\n[TEST] Test Peer Connectivity");
        System.out.print("IP Address to test (e.g., 127.0.0.1): ");
        String ip = scanner.nextLine().trim();
        
        if (ip.isEmpty()) {
            ip = "127.0.0.1";
            System.out.println("Using default IP: " + ip);
        }
        
        System.out.print("Port to test (e.g., 5001): ");
        String portStr = scanner.nextLine().trim();
        
        try {
            int port = Integer.parseInt(portStr);
            ClientHandler.testPeerReachability(ip, port);
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid port number!");
        }
    }
    
    /**
     * Disconnects from the currently connected peer
     */
    private void disconnectFromPeer() {
        if (clientHandler == null || !clientHandler.isConnected()) {
            System.out.println("[NETWORK] Not connected to any peer");
            return;
        }
        
        clientHandler.disconnect();
        System.out.println("[OK] Disconnected from peer");
    }
    
    /**
     * Handles program exit
     */
    private void exitProgram() {
        System.out.println("\n[EXIT] Shutting down P2P peer...");
        running = false;
    }
    
    /**
     * Cleanup method called when program exits
     */
    private void cleanup() {
        try {
            // Disconnect from peer if connected
            if (clientHandler != null && clientHandler.isConnected()) {
                clientHandler.disconnect();
            }
            
            // Stop server thread
            if (serverThread != null) {
                serverThread.stopServer();
                serverThread.join(2000); // Wait up to 2 seconds for clean shutdown
            }
            
            scanner.close();
            System.out.println("[OK] Cleanup completed. Goodbye!");
            
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Main method - entry point of the program
     * @param args Command line arguments (expects port number)
     */
    public static void main(String[] args) {
        // Check if port number is provided
        if (args.length != 1) {
            System.out.println("[ERROR] Usage: java Peer <port>");
            System.out.println("[HELP] Example: java Peer 5000");
            System.out.println("\n[START] Quick Start Guide:");
            System.out.println("1. Terminal 1: java Peer 5000");
            System.out.println("2. Terminal 2: java Peer 5001");
            System.out.println("3. Use menu to connect peers and share files!");
            return;
        }
        
        try {
            int port = Integer.parseInt(args[0]);
            
            // Validate port range
            if (port < 1024 || port > 65535) {
                System.out.println("[ERROR] Port must be between 1024 and 65535");
                return;
            }
            
            // Create and run peer
            Peer peer = new Peer(port);
            peer.run();
            
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid port number: " + args[0]);
            System.out.println("[HELP] Please provide a valid port number (e.g., 5000)");
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 