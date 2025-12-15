package com.flenski.controller;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.flenski.dto.DocumentDto;
import com.flenski.result.QueueResult;
import com.flenski.service.QueueService;

class QueueControllerTest {

    @Mock
    private QueueService queueService;

    @InjectMocks
    private QueueController queueController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void add_shouldReturnBadRequest_whenDocumentsEmpty() {
        ResponseEntity<String> response = queueController.add(Collections.emptyList());
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("No documents provided", response.getBody());
    }

    @Test
    void add_shouldReturnOk_whenDocumentsProvided() {
        DocumentDto doc = new DocumentDto();
        List<DocumentDto> docs = List.of(doc);
        QueueResult queueResult = new QueueResult(1, 0);
        when(queueService.add(docs)).thenReturn(queueResult);

        ResponseEntity<String> response = queueController.add(docs);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Queueing completed - added: 1, duplicates: 0"));
        verify(queueService, times(1)).add(docs);
    }
}
