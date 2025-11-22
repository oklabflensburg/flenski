package com.flenski.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flenski.indexer.IndexerApplication;
import com.flenski.result.QueueResult;
import com.flenski.service.QueueService;

@SpringBootTest(classes = {IndexerApplication.class, QueueControllerTest.MockConfig.class})
@AutoConfigureMockMvc
class QueueControllerTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        public QueueService queueService() {
            return Mockito.mock(QueueService.class);
        }

        @Bean
        public com.flenski.repository.QueueItemRepository queueItemRepository() {
            return Mockito.mock(com.flenski.repository.QueueItemRepository.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QueueService queueService;

        @Test
        void add_returns_ok_when_records_provided() throws Exception {
        // Prepare a valid Record JSON
        String requestBody = "[{" +
            "\"sourceIdentifier\":\"id1\"," +
            "\"sourceName\":\"name\"," +
            "\"sourceUrl\":\"http://example.com\"," +
            "\"sourceDateTime\":\"2023-01-01T00:00:00Z\"," +
            "\"discoveryDateTime\":\"2023-01-01T00:00:00Z\"," +
            "\"sourceType\":\"PDF\"," +
            "\"content\":\"test content\"}]";


        com.flenski.dto.DocumentDto documentDto = new com.flenski.dto.DocumentDto();
        documentDto.setSourceIdentifier("id1");
        documentDto.setSourceName("name");
        documentDto.setSourceUrl("http://example.com");
        documentDto.setSourceDateTime(java.time.Instant.parse("2023-01-01T00:00:00Z"));
        documentDto.setDiscoveryDateTime(java.time.Instant.parse("2023-01-01T00:00:00Z"));
        documentDto.setSourceType(com.flenski.type.SourceType.PDF);
        documentDto.setContent("test content");

        java.util.List<com.flenski.dto.DocumentDto> documentDtos = java.util.List.of(documentDto);
        QueueResult queueResult = new QueueResult(1, 0);
        Mockito.when(queueService.add(Mockito.anyList())).thenReturn(queueResult);

        String expectedResponse = "Queueing completed - added: 1, duplicates: 0";

        mockMvc.perform(post("/api/queue")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResponse));
        }

    @Test
    void add_return_bad_request_when_no_records_providedy() throws Exception {
        mockMvc.perform(post("/api/queue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No documents provided"));
    }
}
