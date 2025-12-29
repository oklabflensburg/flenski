package com.flenski.controller;

import com.flenski.config.VectorStoreClientConfig;
import com.flenski.dto.DocumentDto;
import com.flenski.service.DenseVectorService;
import com.flenski.service.SparseVectorService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Points;
import io.qdrant.client.grpc.Points.QueryPoints;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.qdrant.client.QueryFactory.fusion;
import static io.qdrant.client.QueryFactory.nearest;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final QdrantClient client;
    private final SparseVectorService sparseVectorService;
    private final VectorStoreClientConfig vectorStoreClientConfig;
    private final DenseVectorService denseVectorService;

    public ChatController(
            VectorStoreClientConfig vectorStoreClientConfig,
            SparseVectorService sparseVectorService,
            DenseVectorService denseVectorService

    ) {
        this.client = new QdrantClient(
                QdrantGrpcClient.newBuilder(vectorStoreClientConfig.getHost(), vectorStoreClientConfig.getPort(), false).build()
        );
        this.vectorStoreClientConfig = vectorStoreClientConfig;
        this.sparseVectorService = sparseVectorService;
        this.denseVectorService = denseVectorService;
    }

    @GetMapping("sparsequery")
    public ResponseEntity<List<DocumentDto>> sparsequery(@RequestParam("q") String message) throws Exception {
        Points.SparseVector sparseVector = sparseVectorService.embed(message);

        List<Points.ScoredPoint> scoredPoints = this.client.queryAsync(QueryPoints.newBuilder()
                        .setCollectionName(vectorStoreClientConfig.getCollectionName())
                        .addPrefetch(Points.PrefetchQuery.newBuilder()
                                .setQuery(nearest(sparseVector.getValuesList(), sparseVector.getIndicesList()))
                                .setUsing("sparse")
                                .setLimit(20)
                                .build())
                        .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                        .setQuery(fusion(Points.Fusion.RRF))
                        .build()
                )
                .get();

        List<DocumentDto> documents = scoredPoints.stream().map(DocumentDto::fromScoredPoint).toList();
        return ResponseEntity.ok(documents);
    }
}
