package com.flenski.request;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flenski.dto.Vector;
import com.flenski.service.DenseVectorService;
import com.flenski.service.SparseVectorService;

/**
 * POST http://localhost:6333/dashboard#/collections/points/query { "prefetch":
 * [ { "query": { "indices": [1, 42], // <┐ "values": [0.22, 0.8] // <┴─sparse
 * vector }, "using": "sparse", "limit": 20 }, { "query": [0.01, 0.45, 0.67,
 * ...], // <-- dense vector "using": "dense", "limit": 20 } ], "query": {
 * "fusion": "rrf" }, // <--- reciprocal rank fusion
 */
@Service
public class HybridFusionSearchRequest {
    // ...existing code...
    public String buildJson() {
        Vector sparseVector = sparseVectorService.embed(queryText);
        Vector denseVector = denseVectorService.embed(queryText);

        HashMap<String, Object> sparseQueryMap = new HashMap<>();
        sparseQueryMap.put("query", sparseVector.toQdrantFormat());
        sparseQueryMap.put("using", "sparse");
        sparseQueryMap.put("limit", limit);

        HashMap<String, Object> denseQueryMap = new HashMap<>();
        denseQueryMap.put("query", denseVector.toQdrantFormat());
        denseQueryMap.put("using", "dense");
        denseQueryMap.put("limit", limit);

        ArrayList<HashMap<String, Object>> prefetchList = new ArrayList<>();
        prefetchList.add(sparseQueryMap);
        prefetchList.add(denseQueryMap);

        HashMap<String, Object> queryMap = new HashMap<>();
        queryMap.put("fusion", "rrf");

        HashMap<String, Object> rootMap = new HashMap<>();
        rootMap.put("prefetch", prefetchList);
        rootMap.put("query", queryMap);
        rootMap.put("with_payload", true);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(rootMap);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to build HybridFusionSearchRequest JSON", e);
        }
    }

    private int limit;
    private String queryText;
    private final SparseVectorService sparseVectorService;
    private final DenseVectorService denseVectorService;


    public HybridFusionSearchRequest(SparseVectorService sparseVectorService, DenseVectorService denseVectorService) {
        this.sparseVectorService = sparseVectorService;
        this.denseVectorService = denseVectorService;
    }

    public void setQueryText(String queryText, int limit) {
        this.queryText = queryText;
        this.limit = limit;
    }

    public HttpRequest build() {
        Vector sparseVector = sparseVectorService.embed(queryText);
        Vector denseVector = denseVectorService.embed(queryText);

        HashMap<String, Object> sparseQueryMap = new HashMap<>();
        sparseQueryMap.put("query", sparseVector.toQdrantFormat());
        sparseQueryMap.put("using", "sparse");
        sparseQueryMap.put("limit", limit);

        HashMap<String, Object> denseQueryMap = new HashMap<>();
        denseQueryMap.put("query", denseVector.toQdrantFormat());
        denseQueryMap.put("using", "dense");
        denseQueryMap.put("limit", limit);

        ArrayList<HashMap<String, Object>> prefetchList = new ArrayList<>();
        prefetchList.add(sparseQueryMap);
        prefetchList.add(denseQueryMap);

        HashMap<String, Object> queryMap = new HashMap<>();
        queryMap.put("fusion", "rrf");

        HashMap<String, Object> rootMap = new HashMap<>();
        rootMap.put("prefetch", prefetchList);
        rootMap.put("query", queryMap);
        rootMap.put("with_payload", true);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(rootMap);

            HttpRequest request = HttpRequest.newBuilder()
                    //TODO move this to config
                    .uri(URI.create("http://localhost:6333/collections/hybrid/points/query/"))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            return request;

        } catch (Throwable e) {
            throw new RuntimeException("Failed to build HybridFusionSearchRequest", e);
        }
    }
}
