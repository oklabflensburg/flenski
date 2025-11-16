package com.flenski.service;

import java.util.Collections;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.flenski.dto.Vector;

class SparseVectorServiceTest {

    @Test
    void testVectorizeTF_basic() {
        var vocabulary = new java.util.HashMap<String, Integer>();
        vocabulary.put("foo", 0);
        vocabulary.put("bar", 1);

        VocabularyService vocabularyService = Mockito.mock(VocabularyService.class);
        Mockito.when(vocabularyService.getVocabulary()).thenReturn(Collections.unmodifiableMap(vocabulary));
        Mockito.doNothing().when(vocabularyService).ensureVocabularyFor(Mockito.anyList());

        SparseVectorService service = new SparseVectorService(new WhitespaceAnalyzer(), vocabularyService);

        Vector vector = service.embed("foo bar foo");

        assertNotNull(vector);
        assertEquals(2, vector.getIndices().length);
        assertEquals(2, vector.getValues().length);
        assertArrayEquals(new int[]{0, 1}, vector.getIndices());
    }
}
