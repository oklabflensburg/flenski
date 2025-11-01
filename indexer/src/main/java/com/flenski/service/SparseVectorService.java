package com.flenski.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.stream.IntStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SparseVectorService
{
    private final Analyzer analyzer;
    private final Map<String, Integer> vocabularyCache = new ConcurrentHashMap<>();
    private final File vocabularyFile;
    private final File resultVectorFile;
    private final String resultVectorFilePath;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SparseVectorService(
        Analyzer analyzer,
        @Value("${VOCABULARY_FILE_PATH:./vocabulary.json}") String vocabularyFilePath,
        @Value("${RESULT_VECTOR_FILE_PATH:./result_vectors.txt}") String resultVectorFilePath
    ) {
        this.analyzer = analyzer;
        this.vocabularyFile = new File(vocabularyFilePath);
        this.resultVectorFile = new File(resultVectorFilePath);
        this.resultVectorFilePath = resultVectorFilePath;
        loadVocabFromFile();
    }

    public String getResultVectorFilePath() {
        return resultVectorFilePath;
    }

    public synchronized void ensureVocabularyFor(String text)
    {
        boolean updated = false;
        for (String token : buildTokenList(text))
        {
            if (!vocabularyCache.containsKey(token)) {
                vocabularyCache.put(token, vocabularyCache.size());
                updated = true;
            }
        }
        if (updated) {
            vocabularyToFile();
        }
    }

    private void loadVocabFromFile() {
        if (vocabularyFile.exists()) {
            try (FileReader reader = new FileReader(vocabularyFile)) {
                Map<String, Integer> loaded = objectMapper.readValue(reader, new TypeReference<Map<String, Integer>>() {});
                vocabularyCache.clear();
                vocabularyCache.putAll(loaded);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load vocab cache from file", e);
            }
        }
    }

    private void vocabularyToFile() {
        try (FileWriter writer = new FileWriter(vocabularyFile)) {
            objectMapper.writeValue(writer, vocabularyCache);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save vocab cache to file", e);
        }
    }

    public Map<String, Integer> getVocab()
    {
        return Collections.unmodifiableMap(vocabularyCache);
    }

    /**
     * Creates a sparse vector representation of the input text using TF normalization based on BM25.
     * It does not calculate IDF weights.
     * 
     * @param text
     * @param k1
     * @param b
     * @param avgDocLen
     * @return
     */
    public SparseVec vectorizeTF(String text, double k1, double b, double avgDocLen)
    {
        ensureVocabularyFor(text);

        List<String> tokens = buildTokenList(text);
        int dl = tokens.size();

        Map<String, Integer> tf = new HashMap<>();
        for (String t : tokens)
        {
            tf.merge(t, 1, Integer::sum);
        }

        List<Integer> idx = new ArrayList<>();
        List<Float> val = new ArrayList<>();

        for (Map.Entry<String, Integer> e : tf.entrySet())
        {
            Integer dim = vocabularyCache.get(e.getKey());
            if (dim == null) continue;

            int f = e.getValue();
            double norm = f * (k1 + 1.0) / (f + k1 * (1.0 - b + b * (dl / Math.max(1.0, avgDocLen))));
            if (norm != 0.0)
            {
                idx.add(dim);
                val.add((float) norm);
            }
        }

        List<Integer> order = IntStream.range(0, idx.size())
                .boxed()
                .sorted(Comparator.comparingInt(idx::get))
                .toList();

        int n = order.size();
        int[] indices = new int[n];
        float[] values = new float[n];
        for (int k = 0; k < n; k++)
        {
            int j = order.get(k);
            indices[k] = idx.get(j);
            values[k]  = val.get(j);
        }

        return new SparseVec(indices, values);
    }

    private List<String> buildTokenList(String text)
    {
        List<String> out = new ArrayList<>();
        try (TokenStream ts = analyzer.tokenStream("f", new StringReader(text)))
        {
            CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            while (ts.incrementToken())
            {
                out.add(term.toString());
            }
            ts.end();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Tokenization failed", e);
        }
        return out;
    }

    public record SparseVec(int[] indices, float[] values) {}
}
