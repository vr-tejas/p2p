import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * FileManagerClean - Utility class for handling file operations in the P2P system
 * This class contains methods for:
 * - Listing files in the shared directory
 * - Sending files over network connections
 * - Receiving files from network connections
 */
public class FileManagerClean {
    
    // Directory where shared files are stored
    private static final String SHARED_DIRECTORY = "shared";
    
    /**
     * Gets a list of all files in the shared directory
     * @return List of file names available for sharing
     */
    public static List<String> getAvailableFiles() {
        List<String> files = new ArrayList<>();
        File sharedDir = new File(SHARED_DIRECTORY);
        
        // Check if shared directory exists
        if (!sharedDir.exists()) {
            System.out.println("Shared directory doesn't exist. Creating it...");
            sharedDir.mkdirs();
            return files; // Return empty list if directory was just created
        }
        
        // Get all files from the shared directory
        File[] fileArray = sharedDir.listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                // Only add actual files (not directories)
                if (file.isFile()) {
                    files.add(file.getName());
                }
            }
        }
        
        return files;
    }
    
    /**
     * Sends a file to a connected peer through a socket
     * @param fileName Name of the file to send
     * @param socket Socket connected to the receiving peer
     * @return true if file was sent successfully, false otherwise
     */
    public static boolean sendFile(String fileName, Socket socket) {
        try {
            // Create path to the file in shared directory
            File file = new File(SHARED_DIRECTORY + File.separator + fileName);
            
            // Check if file exists
            if (!file.exists()) {
                System.out.println("File not found: " + fileName);
                return false;
            }
            
            // Get output stream to send data to peer
            OutputStream out = socket.getOutputStream();
            DataOutputStream dataOut = new DataOutputStream(out);
            
            // Send file size first so receiver knows how much to expect
            long fileSize = file.length();
            dataOut.writeLong(fileSize);
            
            // Send the actual file data
            FileInputStream fileIn = new FileInputStream(file);
            BufferedInputStream bufferedIn = new BufferedInputStream(fileIn);
            
            byte[] buffer = new byte[4096]; // 4KB buffer for reading file chunks
            int bytesRead;
            long totalSent = 0;
            
            System.out.println("Sending file: " + fileName + " (Size: " + fileSize + " bytes)");
            
            // Read file in chunks and send over network
            while ((bytesRead = bufferedIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalSent += bytesRead;
                
                // Show progress
                if (totalSent % (1024 * 1024) == 0) { // Every MB
                    System.out.println("Sent: " + totalSent + " bytes");
                }
            }
            
            // Flush to ensure all data is sent
            out.flush();
            
            // Close file streams
            bufferedIn.close();
            fileIn.close();
            
            System.out.println("File sent successfully: " + fileName);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error sending file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Receives a file from a connected peer through a socket
     * @param fileName Name to save the received file as
     * @param socket Socket connected to the sending peer
     * @return true if file was received successfully, false otherwise
     */
    public static boolean receiveFile(String fileName, Socket socket) {
        try {
            // Create downloads directory if it doesn't exist
            File downloadsDir = new File("downloads");
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            
            // Get input stream to receive data from peer
            InputStream in = socket.getInputStream();
            DataInputStream dataIn = new DataInputStream(in);
            
            // First, read the file size
            long fileSize = dataIn.readLong();
            System.out.println("Receiving file: " + fileName + " (Size: " + fileSize + " bytes)");
            
            // Create file to write received data
            File outputFile = new File("downloads" + File.separator + fileName);
            FileOutputStream fileOut = new FileOutputStream(outputFile);
            BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOut);
            
            byte[] buffer = new byte[4096]; // 4KB buffer for receiving file chunks
            long totalReceived = 0;
            int bytesRead;
            
            // Read data from network and write to file
            while (totalReceived < fileSize) {
                // Calculate how much to read (don't read more than remaining file size)
                int toRead = (int) Math.min(buffer.length, fileSize - totalReceived);
                bytesRead = in.read(buffer, 0, toRead);
                
                if (bytesRead == -1) {
                    break; // Connection closed unexpectedly
                }
                
                bufferedOut.write(buffer, 0, bytesRead);
                totalReceived += bytesRead;
                
                // Show progress
                if (totalReceived % (1024 * 1024) == 0) { // Every MB
                    System.out.println("Received: " + totalReceived + " bytes");
                }
            }
            
            // Close file streams
            bufferedOut.close();
            fileOut.close();
            
            System.out.println("File received successfully: " + fileName);
            System.out.println("Saved to: downloads/" + fileName);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error receiving file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Sends a list of available files to a connected peer
     * @param socket Socket connected to the requesting peer
     */
    public static void sendFileList(Socket socket) {
        try {
            List<String> files = getAvailableFiles();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            // Send number of files first
            out.println(files.size());
            
            // Send each file name
            for (String fileName : files) {
                out.println(fileName);
            }
            
            System.out.println("Sent file list to peer (" + files.size() + " files)");
            
        } catch (IOException e) {
            System.err.println("Error sending file list: " + e.getMessage());
        }
    }
    
    /**
     * Receives a list of available files from a connected peer
     * @param socket Socket connected to the peer
     * @return List of file names available on the peer
     */
    public static List<String> receiveFileList(Socket socket) {
        List<String> files = new ArrayList<>();
        
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Read number of files
            int fileCount = Integer.parseInt(in.readLine());
            
            // Read each file name
            for (int i = 0; i < fileCount; i++) {
                String fileName = in.readLine();
                if (fileName != null) {
                    files.add(fileName);
                }
            }
            
            System.out.println("Received file list from peer (" + fileCount + " files)");
            
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error receiving file list: " + e.getMessage());
        }
        
        return files;
    }
    
    /**
     * Creates a sample file in the shared directory for testing
     * @param fileName Name of the file to create
     * @param content Content to write to the file
     */
    public static void createSampleFile(String fileName, String content) {
        try {
            File sharedDir = new File(SHARED_DIRECTORY);
            if (!sharedDir.exists()) {
                sharedDir.mkdirs();
            }
            
            File file = new File(SHARED_DIRECTORY + File.separator + fileName);
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
            
            System.out.println("Sample file created: " + fileName);
            
        } catch (IOException e) {
            System.err.println("Error creating sample file: " + e.getMessage());
        }
    }
} 