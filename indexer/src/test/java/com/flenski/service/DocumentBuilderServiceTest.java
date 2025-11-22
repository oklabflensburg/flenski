package com.flenski.service;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.document.Document;

import com.flenski.config.IndexingConfig;
import com.flenski.dto.DocumentDto;
import com.flenski.type.SourceType;

class DocumentBuilderServiceTest {

    private IndexingConfig config;
    private DocumentBuilderService service;

    @BeforeEach
    void setUp() {
        config = Mockito.mock(IndexingConfig.class);
        Mockito.when(config.getChunkSize()).thenReturn(100);
        Mockito.when(config.getMaxNumnChunks()).thenReturn(5);
        service = new DocumentBuilderService(config);
    }

    @Test
    void testMapDocumentDtoToDocument() {
        DocumentDto dto = new DocumentDto();
        dto.setSourceIdentifier("id-1");
        dto.setSourceName("Test Source");
        dto.setSourceUrl("http://example.com");
        dto.setSourceType(SourceType.TEXT);
        dto.setContent("Hello World");
        dto.setSourceDateTime(Instant.parse("2023-01-01T00:00:00Z"));
        dto.setDiscoveryDateTime(Instant.parse("2023-01-02T00:00:00Z"));

        Document doc = service.mapDocumentDtoToDocument(dto);
        assertNotNull(doc);
        assertEquals("Hello World", doc.getText());
        assertEquals("id-1", doc.getMetadata().get("sourceIdentifier"));
        assertEquals("Test Source", doc.getMetadata().get("sourceName"));
        assertEquals("http://example.com", doc.getMetadata().get("sourceUrl"));
        assertEquals("TEXT", doc.getMetadata().get("sourceType"));
        assertNotNull(doc.getMetadata().get("hash"));
        assertEquals("2023-01-01T00:00:00Z", doc.getMetadata().get("sourceDateTime"));
        assertEquals("2023-01-02T00:00:00Z", doc.getMetadata().get("discoveryDateTime"));
    }

    @Test
    void testToChunkDocuments() {
        DocumentDto dto = new DocumentDto();
        dto.setSourceIdentifier("id-2");
        dto.setSourceName("Chunk Source");
        dto.setSourceUrl("http://example.com/chunk");
        dto.setSourceType(SourceType.TEXT);
        dto.setContent("This is a test document that should be chunked by the splitter.");
        dto.setSourceDateTime(Instant.now());
        dto.setDiscoveryDateTime(Instant.now());

        List<Document> chunks = service.toChunkDocuments(dto);
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        assertTrue(chunks.get(0).getText().contains("This is a test document"));
    }
}
