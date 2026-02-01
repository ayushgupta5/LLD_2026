package src.util;

import java.io.*;
import java.nio.file.*;

// SOLID: Single Responsibility - Only file utility operations
public class FileUtil {

    private static final String METADATA_DIR = "data/metadata";

    // Load counter from file
    public static long loadCounter(String filename, long defaultValue) {
        try {
            Path path = Paths.get(METADATA_DIR, filename);
            if (Files.exists(path)) {
                String content = Files.readString(path).trim();
                return Long.parseLong(content);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Warning: Could not load counter from " + filename +
                    ", using default: " + defaultValue);
        }
        return defaultValue;
    }

    // Save counter to file
    public static void saveCounter(String filename, long value) {
        try {
            Path metadataPath = Paths.get(METADATA_DIR);
            Files.createDirectories(metadataPath);

            Path filePath = Paths.get(METADATA_DIR, filename);
            Files.writeString(filePath, String.valueOf(value));
        } catch (IOException e) {
            System.err.println("Error: Could not save counter to " + filename);
            e.printStackTrace();
        }
    }

    // Ensure directory exists
    public static void ensureDirectoryExists(String directoryPath) {
        try {
            Files.createDirectories(Paths.get(directoryPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory: " + directoryPath, e);
        }
    }

    // Delete file if exists
    public static boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            System.err.println("Error: Could not delete file: " + filePath);
            return false;
        }
    }

    // Check if file exists
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    // Read file content
    public static String readFile(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath));
    }

    // Write file content
    public static void writeFile(String filePath, String content) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    // List all files in directory
    public static File[] listFiles(String directoryPath) {
        File directory = new File(directoryPath);
        return directory.listFiles();
    }

    // List JSON files in directory
    public static File[] listJsonFiles(String directoryPath) {
        File directory = new File(directoryPath);
        return directory.listFiles((dir, name) -> name.endsWith(".json"));
    }

    // Delete directory recursively
    public static void deleteDirectory(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((p1, p2) -> -p1.compareTo(p2)) // Reverse order for deletion
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            System.err.println("Could not delete: " + p);
                        }
                    });
        }
    }

    // Get file size
    public static long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            return -1;
        }
    }

    // Copy file
    public static void copyFile(String sourcePath, String destinationPath) throws IOException {
        Files.copy(Paths.get(sourcePath), Paths.get(destinationPath),
                StandardCopyOption.REPLACE_EXISTING);
    }
}