package com.flenski.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SparseVectorService {

    private static final Logger log = LoggerFactory.getLogger(SparseVectorService.class);
    private final Analyzer analyzer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VocabularyService vocabularyService;

    public SparseVectorService(
            Analyzer analyzer,
            VocabularyService vocabularyService
    ) {
        this.analyzer = analyzer;
        this.vocabularyService = vocabularyService;
    }

    /**
     * Creates a sparse vector representation of the input text using TF
     * normalization based on BM25. It does not calculate IDF weights.
     *
     * @param text
     * @param k1
     * @param b
     * @param avgDocLen
     * @return
     */
    public SparseVector vectorizeTF(String text, double k1, double b, double avgDocLen) {

        List<String> tokens = buildTokenList(text);
        vocabularyService.ensureVocabularyFor(tokens);

        int dl = tokens.size();

        Map<String, Integer> tf = new HashMap<>();
        for (String t : tokens) {
            tf.merge(t, 1, Integer::sum);
        }

    List<Integer> indicesList = new ArrayList<>();
    List<Float> valuesList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : tf.entrySet()) {
            Integer dimension = vocabularyService.getVocabulary().get(entry.getKey());
            if (dimension == null) {
                continue;
            }

            int frequency = entry.getValue();
            double normalization = frequency * (k1 + 1.0) / (frequency + k1 * (1.0 - b + b * (dl / Math.max(1.0, avgDocLen))));
            if (normalization != 0.0) {
                indicesList.add(dimension);
                valuesList.add((float) normalization);
            }
        }

    List<Integer> order = IntStream.range(0, indicesList.size())
        .boxed()
        .sorted(Comparator.comparingInt(indicesList::get))
        .toList();

    int n = order.size();
    int[] indices = new int[n];
    float[] values = new float[n];
    for (int k = 0; k < n; k++) {
        int j = order.get(k);
        indices[k] = indicesList.get(j);
        values[k] = valuesList.get(j);
    }

        return new SparseVector(indices, values);
    }

    private List<String> buildTokenList(String text) {
        List<String> tokens = new ArrayList<>();
        try (TokenStream tokenStream = analyzer.tokenStream("f", new StringReader(text))) {
            CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                tokens.add(term.toString());
            }
            tokenStream.end();
        } catch (Exception e) {
            throw new RuntimeException("Tokenization failed", e);
        }
        return tokens;
    }

    public record SparseVector(int[] indices, float[] values) {

    }
}
