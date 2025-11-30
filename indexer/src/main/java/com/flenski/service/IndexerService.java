package com.flenski.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.flenski.dto.DocumentDto;
import com.flenski.dto.Vector;
import com.flenski.repository.QueueItemRepository;
import com.flenski.result.IndexResult;
import com.flenski.type.SourceType;
import com.flenski.dto.Point;

@Service
public class IndexerService {

    private static final String DENSE_VECTOR_NAME = "dense";
    private static final String SPARSE_VECTOR_NAME = "sparse";

    private static final Logger logger = LoggerFactory.getLogger(IndexerService.class);
    private final DocumentBuilderService documentBuilderService;
    private final DenseVectorService denseVectorService;
    private final SparseVectorService sparseVectorService;
    private final PdfConverterService pdfConverterService;

    public IndexerService(
            DocumentBuilderService documentBuilderService,
            DenseVectorService denseVectorService,
            SparseVectorService sparseVectorService,
            PdfConverterService pdfConverterService
    ) {
        this.documentBuilderService = documentBuilderService;
        this.denseVectorService = denseVectorService;
        this.sparseVectorService = sparseVectorService;
        this.pdfConverterService = pdfConverterService;
    }

    @Async
    public CompletableFuture<DocumentDto> prepareDocumentForIndexing(DocumentDto document) {
        if (document.getSourceType() == SourceType.PDF) {
            return pdfConverterService.convertPdfToDocument(document.getSourceUrl());
        }
        return CompletableFuture.completedFuture(document);
    }

    @Async
    public CompletableFuture<IndexResult> indexAsHybridVector(DocumentDto document) {

        List<Document> documentChunks = documentBuilderService.toChunkDocuments(document);
        for (Document documentChunk : documentChunks) {
            try {
                Vector denseVector = denseVectorService.embed(documentChunk);
                denseVector.setName(DENSE_VECTOR_NAME);

                Vector sparseVector = sparseVectorService.embed(documentChunk.getText());
                sparseVector.setName(SPARSE_VECTOR_NAME);

                Point point = new Point();
                point.build(document, documentChunk.getText(), sparseVector, denseVector);

            } catch (Throwable t) {
                logger.error("Error sending request to Qdrant: {}", t.getMessage(), t);
            }
        }

        return CompletableFuture.completedFuture(new IndexResult(0, 0, 0, 0));
    }
}
