package com.flenski.service;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.flenski.flenski.FlenskiApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.flenski.dto.DocumentDto;
import com.flenski.entity.QueueItem;
import com.flenski.repository.QueueItemRepository;
import com.flenski.result.QueueResult;
import com.flenski.type.SourceType;

@SpringBootTest(classes = FlenskiApplication.class)
class QueueServiceIntegrationTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueItemRepository queueItemRepository;

    private DocumentDto createDocumentDto(String url, String content) {
        DocumentDto dto = new DocumentDto();
        dto.setIdentifier(url);
        dto.setUrl(url);
        dto.setType(SourceType.PDF);
        dto.setDiscoveryDateTime(Instant.now());
        dto.setContent(content);
        return dto;
    }

    @BeforeEach
    void setUp() {
        queueItemRepository.deleteAll();
    }

    @Test
    void add_and_getNext_and_delete() {
        DocumentDto doc1 = createDocumentDto("http://test1", "content1");
        DocumentDto doc2 = createDocumentDto("http://test2", "content2");
        QueueResult result = queueService.add(List.of(doc1, doc2));
        assertEquals(2, result.getAdded());
        assertEquals(0, result.getDuplicates());

        List<QueueItem> items = queueService.getNext(10);
        assertEquals(2, items.size());
        assertEquals(doc1.createHash(), items.get(0).getIdentifier());

        queueService.delete(items.get(0));
        List<QueueItem> afterDelete = queueService.getNext(10);
        assertEquals(1, afterDelete.size());
    }

    @Test
    void add_duplicate_shouldCountAsDuplicate() {
        DocumentDto doc = createDocumentDto("http://dup", "content");
        queueService.add(doc);
        QueueResult result = queueService.add(doc);
        assertEquals(0, result.getAdded());
        assertEquals(1, result.getDuplicates());
    }
}
