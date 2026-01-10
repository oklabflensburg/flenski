package com.flenski.service;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    public File createFileIfMissing(File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();       
            }
        } catch (IOException e) {
            log.error("Failed to create file: {} , {}", file.getAbsolutePath(), e.getMessage());
        }
        return file;
    }

    public File createFileIfMissing(String filePath) {
        return createFileIfMissing(new File(filePath));
    }

    public boolean fileExists(File file) {
        return file.exists();
    }

    public boolean isFileEmpty(File file) {
        return file.length() == 0;
    }

    public void writeToFile(File file, String content) {
        try (var writer = new java.io.FileWriter(file)) {
            writer.write(content);
        } catch (IOException e) {
            log.error("Failed to write to file: {} , {}", file.getAbsolutePath(), e.getMessage());
        }
    }
}
