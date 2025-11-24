package com.flenski.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.dao.DataIntegrityViolationException;

import com.flenski.dto.DocumentDto;
import com.flenski.entity.QueueItem;
import com.flenski.repository.QueueItemRepository;
import com.flenski.result.QueueResult;

class QueueServiceTest {

    private QueueItemRepository queueItemRepository;
    private QueueService queueService;

    @BeforeEach
    void setUp() {
        queueItemRepository = mock(QueueItemRepository.class);
        queueService = new QueueService(queueItemRepository);
    }

    @Test
    void a_document_can_be_added() {
        DocumentDto dto = mock(DocumentDto.class);
        when(dto.createHash()).thenReturn("hash1");
        QueueResult result = queueService.add(dto);
        assertEquals(1, result.getAdded());
        assertEquals(0, result.getDuplicates());
        verify(queueItemRepository, times(1)).save(any(QueueItem.class));
    }

    @Test
    void a_dupllicated_docuemnt_will_not_be_added() {
        DocumentDto dto = mock(DocumentDto.class);
        when(dto.createHash()).thenReturn("hash2");
        doThrow(new DataIntegrityViolationException("duplicate")).when(queueItemRepository).save(any(QueueItem.class));
        QueueResult result = queueService.add(dto);
        assertEquals(0, result.getAdded());
        assertEquals(1, result.getDuplicates());
    }


    @Test
    void testGetNext() {
        List<QueueItem> items = List.of(mock(QueueItem.class));
        when(queueItemRepository.findAllByOrderByCreatedAtAsc(any())).thenReturn(items);
        List<QueueItem> result = queueService.getNext(5);
        assertEquals(items, result);
    }

    @Test
    void testDelete() {
        QueueItem item = mock(QueueItem.class);
        queueService.delete(item);
        verify(queueItemRepository, times(1)).delete(item);
    }
}
