package com.flenski.controller;

import com.flenski.config.QueryConfig;
import com.flenski.config.VectorStoreClientConfig;
import com.flenski.dto.DocumentDto;
import com.flenski.dto.QueryParameterBag;
import com.flenski.queryTransformers.CompressionTransformer;
import com.flenski.queryTransformers.DateRangeTransformer;
import com.flenski.service.DenseVectorService;
import com.flenski.service.QueryService;
import com.flenski.service.SparseVectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ChatControllerTest {

    @Mock private VectorStoreClientConfig vectorStoreClientConfig;
    @Mock private SparseVectorService sparseVectorService;
    @Mock private DenseVectorService denseVectorService;
    @Mock private CompressionTransformer compressionTransformer;
    @Mock private DateRangeTransformer dateRangeTransformer;
    @Mock private ChatClient chatClient;
    @Mock private QueryService queryService;
    @Mock private QueryConfig queryConfig;

    private ChatController chatController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(vectorStoreClientConfig.getHost()).thenReturn("localhost");
        when(vectorStoreClientConfig.getPort()).thenReturn(6334);
        when(vectorStoreClientConfig.getCollectionName()).thenReturn("test-collection");
        when(vectorStoreClientConfig.getApiKey()).thenReturn(null);

        chatController = new ChatController(
                vectorStoreClientConfig,
                sparseVectorService,
                denseVectorService,
                compressionTransformer,
                dateRangeTransformer,
                chatClient,
                queryService,
                queryConfig
        );
    }

    private void stubQueryConfig() {
        when(queryConfig.getQueryMode()).thenReturn(QueryParameterBag.QueryMode.HYBRID);
        when(queryConfig.getEnableTimeBoost()).thenReturn(false);
        when(queryConfig.getLimit()).thenReturn(10);
        when(queryConfig.getMaxLimit()).thenReturn(50);
        when(queryConfig.getTimeBoostScale()).thenReturn(30);
        when(queryConfig.getEnableTitleBoost()).thenReturn(false);
        when(queryConfig.getTitleBoostFactor()).thenReturn(1.0f);
        when(queryConfig.getSparseSearchScoreThreshold()).thenReturn(0.1f);
        when(queryConfig.getDenseSearchScoreThreshold()).thenReturn(0.2f);
    }

    @Test
    void defaultParameters_shouldReturnQueryParameterBagInitializedFromConfig() {
        stubQueryConfig();

        QueryParameterBag result = chatController.defaultParameters();

        assertNotNull(result);
        assertEquals(QueryParameterBag.QueryMode.HYBRID, result.getQueryMode());
        assertEquals(10, result.getLimit());
        assertFalse(result.getEnableTimeBoost());
    }

    @Test
    void postQuery_shouldReturnSseEmitter() throws Exception {
        stubQueryConfig();
        when(queryService.query(any(), eq("flenski test"), any(), any()))
                .thenReturn(List.of(new DocumentDto()));

        SseEmitter emitter = chatController.postQuery("flenski test", new QueryParameterBag());

        assertNotNull(emitter);
    }
}

