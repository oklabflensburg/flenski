package com.flenski.assistant.config;

import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {

        ChatOptions chatOptions = ChatOptions.builder()
                .model("gpt-3.5-turbo")
                .maxTokens(400)
                .temperature(0.1)
                .build();

        return chatClientBuilder
            .defaultOptions(chatOptions)
            .build();
    }
}
