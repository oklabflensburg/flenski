package com.flenski.service;

import com.flenski.dto.DocumentDto;
import com.flenski.dto.Vector;
import com.flenski.result.IndexResult;
import com.flenski.type.SourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.document.Document;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IndexerServiceTest {

    @Mock
    private DocumentBuilderService documentBuilderService;
    @Mock
    private DenseVectorService denseVectorService;
    @Mock
    private SparseVectorService sparseVectorService;
    @Mock
    private PdfConverterService pdfConverterService;

    @InjectMocks
    private IndexerService indexerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void prepareDocumentForIndexing_nonPdf_returnsCompletedFuture() throws Exception {
        DocumentDto doc = mock(DocumentDto.class);
        when(doc.getSourceType()).thenReturn(SourceType.TEXT);
        CompletableFuture<DocumentDto> result = indexerService.prepareDocumentForIndexing(doc);
        assertEquals(doc, result.get());
        verify(pdfConverterService, never()).convertPdfToDocument(any());
    }

    @Test
    void prepareDocumentForIndexing_pdf_callsPdfConverter() throws Exception {
        DocumentDto doc = mock(DocumentDto.class);
        when(doc.getSourceType()).thenReturn(SourceType.PDF);
        when(doc.getSourceUrl()).thenReturn("file.pdf");
        CompletableFuture<DocumentDto> future = new CompletableFuture<>();
        when(pdfConverterService.convertPdfToDocument("file.pdf")).thenReturn(future);
        CompletableFuture<DocumentDto> result = indexerService.prepareDocumentForIndexing(doc);
        assertSame(future, result);
        verify(pdfConverterService).convertPdfToDocument("file.pdf");
    }

    @Test
    void indexAsHybridVector_successful() throws Exception {
        DocumentDto doc = mock(DocumentDto.class);
        Document chunk = mock(Document.class);
        when(documentBuilderService.toChunkDocuments(doc)).thenReturn(Collections.singletonList(chunk));
        Vector dense = mock(Vector.class);
        Vector sparse = mock(Vector.class);
        when(denseVectorService.embed(chunk)).thenReturn(dense);
        when(sparseVectorService.embed(anyString())).thenReturn(sparse);
        when(chunk.getText()).thenReturn("text");
        IndexResult result = indexerService.indexAsHybridVector(doc).get();
        assertNotNull(result);
        verify(denseVectorService).embed(chunk);
        verify(sparseVectorService).embed("text");
    }
}
