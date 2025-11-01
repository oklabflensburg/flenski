package com.flenski.service;

import java.util.Map;

import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SparseVectorServiceTest {
    private SparseVectorService service;

    private final String vocabFilePath = "./build/test-vocab.json";
    private final String resultVectorFilePath = "./build/test-result_vectors.txt";

    @BeforeEach
    void setUp() {
        service = new SparseVectorService(
            new GermanAnalyzer(),
            vocabFilePath,
            resultVectorFilePath
        );
    }

    @AfterEach
    void cleanUp() {
        java.io.File vocabFile = new java.io.File(vocabFilePath);
        java.io.File resultFile = new java.io.File(resultVectorFilePath);
        if (vocabFile.exists()) vocabFile.delete();
        if (resultFile.exists()) resultFile.delete();
    }

    @Test
    void testEnsureVocabulary_addsTokens() {
    String text = "Flensburg ist eine Stadt.";
    service.ensureVocabularyFor(text);
    Map<String, Integer> vocab = service.getVocab();
    assertTrue(vocab.containsKey("flensburg"));
    assertTrue(vocab.containsKey("stadt"));
    }

    @Test
    void testVectorizeTF_returnsSparseVec() {
    String text = "Flensburg ist eine Stadt.";
    service.ensureVocabularyFor(text);
    SparseVectorService.SparseVec vector = service.vectorizeTF(text, 1.2, 0.75, 100.0);
    assertNotNull(vector);
    assertTrue(vector.indices().length > 0);
    assertTrue(vector.values().length > 0);
    assertEquals(vector.indices().length, vector.values().length);
    }
}
