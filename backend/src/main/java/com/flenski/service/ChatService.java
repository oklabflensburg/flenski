package com.flenski.service;

import com.flenski.advisors.TokenUsageAuditAdvisor;
import com.flenski.dto.DocumentDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    final private ChatClient chatClient;

    @Value("${flenski.chat.context.documentLimit:100}")
    private int documentContextLimit;

    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    private Resource systemPromptTemplate;

    private TokenUsageAuditAdvisor tokenUsageAuditAdvisor;

    public ChatService(ChatClient chatClient, TokenUsageAuditAdvisor tokenUsageAuditAdvisor) {
        this.chatClient = chatClient;
        this.tokenUsageAuditAdvisor = tokenUsageAuditAdvisor;
    }

    public String ask(String question, List<DocumentDto> documents) {
        if (documents.isEmpty()) {
            return null;
        }

        return chatClient.prompt()
                .system(promptTemplateSpec -> promptTemplateSpec
                    .text(systemPromptTemplate)
                    .param("documents", buildContext(documents))
                )
                .user(question)
                .advisors(tokenUsageAuditAdvisor)
                .call()
                .content();
    }

    private String buildContext(List<DocumentDto> documents) {
        return documents.stream()
                .limit(documentContextLimit)
                .map(DocumentDto::getContent)
                .reduce("", (a, b) -> a + "\n" + b);
    }
}
