package com.flenski.assistant.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:9001")
@RequestMapping("/api/")
public class ChatController {

    private final ChatClient chatClient;

    private final VectorStore vectorStore;

    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource systemPromptTemplate;

    private final Logger log = LoggerFactory.getLogger(ChatController.class);

    public ChatController(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @GetMapping("/chat")
    public ResponseEntity<Map<String, Object>>chat(@RequestParam("message") String message) {

        SearchRequest searchRequest
                = SearchRequest.builder()
                        .query(message)
                        .topK(5)
                        .similarityThreshold(0.5)
                        .build();

        List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);

        StringBuilder documentContext = new StringBuilder();
        String sources = "";

        for (Document doc : similarDocs) {
            documentContext.append(doc.getText()).append(System.lineSeparator());
            sources += "Dokument: " + doc.getMetadata().get("identifier") + "\n";
            sources += "Datum: " + doc.getMetadata().get("date") + "\n";
            sources += "URL: " + doc.getMetadata().get("url") + "\n";
            sources += "gesichtet am: " + doc.getMetadata().get("indexingDate") + "\n\n";
        }

        log.debug("Request:" + message);

        String answer = chatClient.prompt()
                .system(promptTemplateSpec -> promptTemplateSpec
                .text(systemPromptTemplate)
                .param("documents", documentContext))
                .user(message)
                .call()
                .content();


        log.debug("Answer: " + answer);

        Map<String, Object> response = new HashMap<>();
        response.put("answer", answer);
        response.put("sources", similarDocs);
        return ResponseEntity.ok(response);
    }
}
