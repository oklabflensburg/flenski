package com.flenski.assistant.controller;

import java.util.List;
import java.util.stream.Collectors;

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

import com.flenski.assistant.dto.ChatResponse;
import com.flenski.assistant.dto.SourceInfo;
import com.flenski.assistant.queryTransformers.TranslationTransformer;

@RestController
@CrossOrigin(origins = "http://localhost:9001")
@RequestMapping("/api/")
public class ChatController {

    private final ChatClient chatClient;

    private final VectorStore vectorStore;

    private final TranslationTransformer translationTransformer;

    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource systemPromptTemplate;

    public ChatController(
        ChatClient chatClient, 
        VectorStore vectorStore, 
        TranslationTransformer translationTransformer) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.translationTransformer = translationTransformer;
    }

    @GetMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestParam("message") String message) {

        String translatedQuery = translationTransformer.transform(message);      

        SearchRequest searchRequest = SearchRequest.builder()
                        .query(translatedQuery)
                        .topK(5)
                        .similarityThreshold(0.5)
                        .build();

        List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);

        StringBuilder documentContext = new StringBuilder();
    List<SourceInfo> sourceInfos = similarDocs
        .stream()
        .map(doc -> {
            documentContext.append(doc.getText()).append(System.lineSeparator());
            String url = String.valueOf(doc.getMetadata().getOrDefault("sourceUrl", ""));
            String identifier = String.valueOf(doc.getMetadata().getOrDefault("sourceIdentifier", ""));
            String type = String.valueOf(doc.getMetadata().getOrDefault("sourceType", ""));
            String name = String.valueOf(doc.getMetadata().getOrDefault("sourceName", ""));
            String date = String.valueOf(doc.getMetadata().getOrDefault("sourceDate", ""));
            String discoveryDateTime = String.valueOf(doc.getMetadata().getOrDefault("discoveryDateTime", ""));
            String distance = String.valueOf(doc.getMetadata().getOrDefault("distance", ""));                    
            String hash = String.valueOf(doc.getMetadata().getOrDefault("hash", ""));
            return new SourceInfo(url, identifier, type, name, date, discoveryDateTime, distance, hash);
        })
        .collect(Collectors.toMap(
            SourceInfo::getIdentifier,
            s -> s,
            (existing, replacement) -> existing
        ))
        .values()
        .stream()
        .collect(Collectors.toList());

        String answer = chatClient.prompt()
                .system(promptTemplateSpec -> promptTemplateSpec
                .text(systemPromptTemplate)
                .param("documents", documentContext)
                )
                .user(message)
                .call()
                .content();
         
        ChatResponse response = new ChatResponse(answer, sourceInfos);
        return ResponseEntity.ok(response);
    }
}
