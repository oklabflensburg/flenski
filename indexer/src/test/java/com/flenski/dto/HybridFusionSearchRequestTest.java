package com.flenski.dto;

import com.flenski.request.HybridFusionSearchRequest;
import com.flenski.service.DenseVectorService;
import com.flenski.service.SparseVectorService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HybridFusionSearchRequestTest {
    @Test
    void testBuildReturnsValidJson() {
        SparseVectorService sparseService = mock(SparseVectorService.class);
        DenseVectorService denseService = mock(DenseVectorService.class);
        Vector sparseVector = mock(Vector.class);
        Vector denseVector = mock(Vector.class);
        when(sparseService.embed(anyString())).thenReturn(sparseVector);
        when(denseService.embed(anyString())).thenReturn(denseVector);
        when(sparseVector.toQdrantFormat()).thenReturn(new int[]{1, 2});
        when(denseVector.toQdrantFormat()).thenReturn(new double[]{0.1, 0.2});

        HybridFusionSearchRequest request = new HybridFusionSearchRequest(sparseService, denseService);
        request.setQueryText("test", 10);
        String json = request.build();
        assertEquals("{\"prefetch\":[{\"using\":\"sparse\",\"query\":[1,2],\"limit\":10},{\"using\":\"dense\",\"query\":[0.1,0.2],\"limit\":10}],\"query\":{\"fusion\":\"rrf\"}}", json);
    }
}
