package com.flenski.service;

import java.io.File;
import java.util.Map;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VocabularyService {

    private final File vocabularyFile;
    private final FileService fileService;
    private final Map<String, Integer> vocabularyCache = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(VocabularyService.class);

    public VocabularyService(
            @Value("${VOCABULARY_FILE_PATH:./vocabulary.json}") String vocabularyFilePath,
            FileService fileService) {

        this.fileService = fileService;
        this.vocabularyFile = fileService.createFileIfMissing(vocabularyFilePath);
        loadVocabulary();
    }

    public Map<String, Integer> getVocabulary() {
        return Collections.unmodifiableMap(vocabularyCache);
    }

    private void loadVocabulary() {
        if (fileService.fileExists(vocabularyFile) && !fileService.isFileEmpty(vocabularyFile)) {
            try (FileReader reader = new FileReader(vocabularyFile)) {
                Map<String, Integer> loaded = objectMapper.readValue(reader, new TypeReference<Map<String, Integer>>() {
                });
                vocabularyCache.clear();
                vocabularyCache.putAll(loaded);
            } catch (IOException e) {
                log.error("Failed to load vocabulary cache from file: {}", e.getMessage());
            }
        }
    }

    public synchronized void ensureVocabularyFor(List<String> tokens) {
        boolean updated = false;
        for (String token : tokens) {
            if (!vocabularyCache.containsKey(token)) {
                vocabularyCache.put(token, vocabularyCache.size());
                updated = true;
            }
        }
        if (updated) {
            persistVocabulary();
        }
    }

    public void persistVocabulary() {
        try {
            String json = objectMapper.writeValueAsString(vocabularyCache);
            fileService.writeToFile(vocabularyFile, json);
        } catch (IOException e) {
            log.error("Failed to save vocabulary cache to file: {}", e.getMessage());
        }
    }
}
