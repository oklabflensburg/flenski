package com.flenski.service;

import com.flenski.config.QueryConfig;
import com.flenski.config.VectorStoreClientConfig;
import com.flenski.dto.DocumentDto;
import com.flenski.dto.QueryParameterBag;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;

import com.google.common.util.concurrent.Futures;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QueryServiceTest {

    @Mock private VectorStoreClientConfig vectorStoreClientConfig;
    @Mock private SparseVectorService sparseVectorService;
    @Mock private DenseVectorService denseVectorService;
    @Mock private ChatClient chatClient;
    @Mock private QdrantClient qdrantClient;
    @Mock private QueryConfig queryConfig;

    private QueryService queryService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        queryService = new QueryService(vectorStoreClientConfig, sparseVectorService, denseVectorService, chatClient);

        when(queryConfig.getCollection(any())).thenReturn("test-collection");
        when(queryConfig.getTimeBoostMidpoint()).thenReturn(0.5f);
        when(queryConfig.getTimeBoostDateField()).thenReturn("source_date_time");
        when(qdrantClient.queryAsync(any())).thenReturn(Futures.immediateFuture(List.of()));
    }

    // --- query mode routing ---

    @Test
    void query_lexicalMode_shouldOnlyEmbedSparseVector() throws Exception {
        when(sparseVectorService.embed(any())).thenReturn(Points.SparseVector.newBuilder().build());

        queryService.query(qdrantClient, "flenski", bagWithMode(QueryParameterBag.QueryMode.LEXICAL), queryConfig);

        verify(sparseVectorService, times(1)).embed("flenski");
        verify(denseVectorService, never()).embed(anyString());
    }

    @Test
    void query_semanticMode_shouldOnlyEmbedDenseVector() throws Exception {
        when(denseVectorService.embed(any(String.class))).thenReturn(Points.DenseVector.newBuilder().build());

        queryService.query(qdrantClient, "flenski", bagWithMode(QueryParameterBag.QueryMode.SEMANTIC), queryConfig);

        verify(denseVectorService, times(1)).embed("flenski");
        verify(sparseVectorService, never()).embed(anyString());
    }

    @Test
    void query_hybridMode_shouldEmbedBothVectors() throws Exception {
        when(sparseVectorService.embed(any())).thenReturn(Points.SparseVector.newBuilder().build());
        when(denseVectorService.embed(any(String.class))).thenReturn(Points.DenseVector.newBuilder().build());

        queryService.query(qdrantClient, "flenski", bagWithMode(QueryParameterBag.QueryMode.HYBRID), queryConfig);

        verify(sparseVectorService, times(1)).embed("flenski");
        verify(denseVectorService, times(1)).embed("flenski");
    }

    // --- result mapping ---

    @Test
    void query_shouldReturnMappedDocumentWithScore() throws Exception {
        when(denseVectorService.embed(any(String.class))).thenReturn(Points.DenseVector.newBuilder().build());
        Points.ScoredPoint scoredPoint = Points.ScoredPoint.newBuilder().setScore(0.85f).build();
        when(qdrantClient.queryAsync(any())).thenReturn(Futures.immediateFuture(List.of(scoredPoint)));

        List<DocumentDto> result = queryService.query(qdrantClient, "flenski", bagWithMode(QueryParameterBag.QueryMode.SEMANTIC), queryConfig);

        assertEquals(1, result.size());
        assertEquals(0.85, result.get(0).getScore(), 0.001);
    }

    @Test
    void query_shouldReturnEmptyList_whenNoResultsFound() throws Exception {
        when(denseVectorService.embed(any(String.class))).thenReturn(Points.DenseVector.newBuilder().build());
        when(qdrantClient.queryAsync(any())).thenReturn(Futures.immediateFuture(List.of()));

        List<DocumentDto> result = queryService.query(qdrantClient, "no match", bagWithMode(QueryParameterBag.QueryMode.SEMANTIC), queryConfig);

        assertTrue(result.isEmpty());
    }

    @Test
    void query_shouldReturnMultipleDocuments() throws Exception {
        when(denseVectorService.embed(any(String.class))).thenReturn(Points.DenseVector.newBuilder().build());
        List<Points.ScoredPoint> points = List.of(
                Points.ScoredPoint.newBuilder().setScore(0.9f).build(),
                Points.ScoredPoint.newBuilder().setScore(0.7f).build(),
                Points.ScoredPoint.newBuilder().setScore(0.5f).build()
        );
        when(qdrantClient.queryAsync(any())).thenReturn(Futures.immediateFuture(points));

        List<DocumentDto> result = queryService.query(qdrantClient, "flenski", bagWithMode(QueryParameterBag.QueryMode.SEMANTIC), queryConfig);

        assertEquals(3, result.size());
    }

    // --- helper ---

    private QueryParameterBag bagWithMode(QueryParameterBag.QueryMode mode) {
        QueryParameterBag bag = new QueryParameterBag();
        bag.setQueryMode(mode);
        bag.setEnableTimeBoost(false);
        bag.setEnableTitleBoost(false);
        bag.setLimit(10);
        bag.setSparseSearchScoreThreshold(0.1f);
        bag.setDenseSearchScoreThreshold(0.1f);
        return bag;
    }
}

