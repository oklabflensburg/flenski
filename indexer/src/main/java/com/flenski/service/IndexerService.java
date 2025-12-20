package com.flenski.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.flenski.config.VectorStoreClientConfig;
import com.flenski.dto.DocumentDto;
import com.flenski.type.SourceType;

import io.qdrant.client.PointIdFactory;
import io.qdrant.client.QdrantClient;
import static io.qdrant.client.ValueFactory.value;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.DenseVector;
import io.qdrant.client.grpc.Points.NamedVectors;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.SparseVector;
import io.qdrant.client.grpc.Points.Vector;
import io.qdrant.client.grpc.Points.Vectors;

@Service
public class IndexerService {

    private static final String DENSE_VECTOR_NAME = "dense";
    private static final String SPARSE_VECTOR_NAME = "sparse";

    private static final Logger logger = LoggerFactory.getLogger(IndexerService.class);

    private final DocumentBuilderService documentBuilderService;
    private final DenseVectorService denseVectorService;
    private final SparseVectorService sparseVectorService;
    private final PdfConverterService pdfConverterService;
    private final QdrantClient client;
    private final VectorService vectorService;

    public IndexerService(
            DocumentBuilderService documentBuilderService,
            DenseVectorService denseVectorService,
            SparseVectorService sparseVectorService,
            PdfConverterService pdfConverterService,
            VectorService vectorService,
            VectorStoreClientConfig vectorStoreClientConfig
    ) {
        this.documentBuilderService = documentBuilderService;
        this.denseVectorService = denseVectorService;
        this.sparseVectorService = sparseVectorService;
        this.pdfConverterService = pdfConverterService;
        this.vectorService = vectorService;
        this.client = vectorStoreClientConfig.vectorStoreClient();
    }

    @Async
    public CompletableFuture<DocumentDto> prepareDocumentForIndexing(DocumentDto document) {
        if (document.getType() == SourceType.PDF) {
            return pdfConverterService.convertPdfToDocument(document.getUrl());
        }
        return CompletableFuture.completedFuture(document);
    }

    public CompletableFuture<String> upsert(DocumentDto document) {
        List<Document> documentChunks = documentBuilderService.toChunkDocuments(document);
        List<PointStruct> points = new ArrayList<>();

        for (Document documentChunk : documentChunks) {
            DenseVector denseVector = denseVectorService.embed(documentChunk);
            SparseVector sparseVector = sparseVectorService.embed(documentChunk.getText());

            NamedVectors namedVectors = NamedVectors.newBuilder()
                    .putAllVectors(
                            Map.of(
                                    DENSE_VECTOR_NAME,
                                    Vector.newBuilder().setDense(denseVector).build(),
                                    SPARSE_VECTOR_NAME,
                                    Vector.newBuilder().setSparse(sparseVector).build()
                            )
                    )
                    .build();

            Vectors vectors = Vectors.newBuilder().setVectors(namedVectors).build();

            UUID uuid = this.vectorService.uuid(denseVector);
            PointStruct point = PointStruct.newBuilder()
                    .setVectors(vectors)
                    .putAllPayload(buildPayload(document, documentChunk))
                    .setId(PointIdFactory.id(uuid))
                    .build();

            points.add(point);
        }
        this.client.upsertAsync("test", points);
        return CompletableFuture.completedFuture("ok");
    }

    Map<String, Value> buildPayload(DocumentDto document, Document chunk) {
        return Map.of(
                "url", value(document.getUrl()),
                "identifier", value(document.getIdentifier() != null ? document.getIdentifier() : ""),
                "title", value(document.getTitle() != null ? document.getTitle() : ""),
                "description", value(document.getDescription() != null ? document.getDescription() : ""),
                "summary", value(document.getSummary() != null ? document.getSummary() : ""),
                "type", value(document.getType().toString()),
                "discovery_date_time", value(document.getDiscoveryDateTime() != null ? document.getDiscoveryDateTime().toString() : ""),
                "source_date_time", value(document.getSourceDateTime() != null ? document.getSourceDateTime().toString() : ""),
                "content", value(chunk.getText()
                )
        );
    }

}
