package com.example.demo.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for file operations.
 * 
 * Provides methods for file upload, download, validation,
 * and management used throughout the application.
 */
@Slf4j
@UtilityClass
public class FileUtil {
    
    // Allowed file extensions for different file types
    public static final List<String> EXCEL_EXTENSIONS = Arrays.asList(".xlsx", ".xls");
    public static final List<String> IMAGE_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp");
    public static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(".pdf", ".doc", ".docx", ".txt");
    
    // Maximum file sizes (in bytes)
    public static final long MAX_EXCEL_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final long MAX_IMAGE_FILE_SIZE = 5 * 1024 * 1024;  // 5MB
    public static final long MAX_DOCUMENT_FILE_SIZE = 20 * 1024 * 1024; // 20MB
    
    /**
     * Validates if a file is a valid Excel file
     */
    public static boolean isValidExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        return EXCEL_EXTENSIONS.contains(extension) && 
               file.getSize() <= MAX_EXCEL_FILE_SIZE;
    }
    
    /**
     * Validates if a file is a valid image file
     */
    public static boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        return IMAGE_EXTENSIONS.contains(extension) && 
               file.getSize() <= MAX_IMAGE_FILE_SIZE;
    }
    
    /**
     * Validates if a file is a valid document file
     */
    public static boolean isValidDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        return DOCUMENT_EXTENSIONS.contains(extension) && 
               file.getSize() <= MAX_DOCUMENT_FILE_SIZE;
    }
    
    /**
     * Gets the file extension from a filename
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
    
    /**
     * Gets the filename without extension
     */
    public static String getFilenameWithoutExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(0, lastDotIndex) : filename;
    }
    
    /**
     * Generates a unique filename with timestamp
     */
    public static String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("%s_%s%s", timestamp, uuid, extension);
    }
    
    /**
     * Saves a multipart file to the specified directory
     */
    public static String saveFile(MultipartFile file, String uploadDirectory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = generateUniqueFilename(originalFilename);
        
        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("File saved: {} -> {}", originalFilename, uniqueFilename);
        return uniqueFilename;
    }
    
    /**
     * Reads a file as byte array
     */
    public static byte[] readFileAsBytes(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        
        return Files.readAllBytes(path);
    }
    
    /**
     * Reads a file as string
     */
    public static String readFileAsString(String filePath) throws IOException {
        byte[] bytes = readFileAsBytes(filePath);
        return new String(bytes);
    }
    
    /**
     * Writes content to a file
     */
    public static void writeStringToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        // Create parent directories if they don't exist
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        Files.write(path, content.getBytes());
        log.info("Content written to file: {}", filePath);
    }
    
    /**
     * Writes byte array to a file
     */
    public static void writeBytesToFile(byte[] bytes, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        // Create parent directories if they don't exist
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        Files.write(path, bytes);
        log.info("Bytes written to file: {}", filePath);
    }
    
    /**
     * Deletes a file
     */
    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("File deleted: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * Checks if a file exists
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * Gets file size in bytes
     */
    public static long getFileSize(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        
        return Files.size(path);
    }
    
    /**
     * Formats file size in human-readable format
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Creates a temporary file with the given content
     */
    public static String createTempFile(String content, String prefix, String suffix) throws IOException {
        Path tempFile = Files.createTempFile(prefix, suffix);
        Files.write(tempFile, content.getBytes());
        
        String tempFilePath = tempFile.toString();
        log.info("Temporary file created: {}", tempFilePath);
        
        return tempFilePath;
    }
    
    /**
     * Creates a temporary file from MultipartFile
     */
    public static String createTempFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String prefix = getFilenameWithoutExtension(originalFilename);
        
        Path tempFile = Files.createTempFile(prefix, extension);
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        
        String tempFilePath = tempFile.toString();
        log.info("Temporary file created from upload: {}", tempFilePath);
        
        return tempFilePath;
    }
    
    /**
     * Cleans up old temporary files
     */
    public static void cleanupTempFiles(String tempDirectory, int maxAgeHours) {
        try {
            Path tempDir = Paths.get(tempDirectory);
            if (!Files.exists(tempDir)) {
                return;
            }
            
            long cutoffTime = System.currentTimeMillis() - (maxAgeHours * 60 * 60 * 1000L);
            
            Files.walk(tempDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("Cleaned up old temp file: {}", path);
                        } catch (IOException e) {
                            log.warn("Failed to delete old temp file: {}", path, e);
                        }
                    });
                    
        } catch (IOException e) {
            log.error("Failed to cleanup temp files in directory: {}", tempDirectory, e);
        }
    }
}