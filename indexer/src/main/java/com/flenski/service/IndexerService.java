package com.flenski.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.flenski.dto.DocumentDto;
import com.flenski.dto.Point;
import com.flenski.dto.Vector;
import com.flenski.request.IndexRequest;
import com.flenski.result.IndexResult;
import com.flenski.type.SourceType;
import java.net.http.HttpResponse;


@Service
public class IndexerService {

    private static final String DENSE_VECTOR_NAME = "dense";
    private static final String SPARSE_VECTOR_NAME = "sparse";

    private static final Logger logger = LoggerFactory.getLogger(IndexerService.class);
    private final DocumentBuilderService documentBuilderService;
    private final DenseVectorService denseVectorService;
    private final SparseVectorService sparseVectorService;
    private final PdfConverterService pdfConverterService;
    private final HttpService httpService;

    public IndexerService(
            DocumentBuilderService documentBuilderService,
            DenseVectorService denseVectorService,
            SparseVectorService sparseVectorService,
            PdfConverterService pdfConverterService,
            HttpService httpService
    ) {
        this.documentBuilderService = documentBuilderService;
        this.denseVectorService = denseVectorService;
        this.sparseVectorService = sparseVectorService;
        this.pdfConverterService = pdfConverterService;
        this.httpService = httpService;
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
        IndexRequest indexRequest = new IndexRequest();
        for (Document documentChunk : documentChunks) {
            try {
                Vector denseVector = denseVectorService.embed(documentChunk);
                denseVector.setName(DENSE_VECTOR_NAME);

                Vector sparseVector = sparseVectorService.embed(documentChunk.getText());
                sparseVector.setName(SPARSE_VECTOR_NAME);

                Point point = new Point();
                point.build(document, documentChunk.getText(), sparseVector, denseVector);

                indexRequest.addPoint(point);

            } catch (Throwable t) {
                logger.error("Error sending request to Qdrant: {}", t.getMessage(), t);
            }
        }

        HttpResponse<String> response = httpService.sendRequest(indexRequest);

        return CompletableFuture.completedFuture(new IndexResult(0, 0, 0, 0));
    }

}
