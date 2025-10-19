package com.flenski.assistant.queryTransformers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.stereotype.Service;

@Service

public class TranslationTransformer {

    private static final Logger logger = LoggerFactory.getLogger(TranslationTransformer.class);
    private final ChatClient.Builder chatClientBuilder;

    public TranslationTransformer(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public String transform(String message) {

        logger.info("Translationtransformer called.");
        Query query = Query.builder()
                .text(message)
                .build();

        TranslationQueryTransformer queryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder.clone())
                .targetLanguage("deutsch")
                .build();

        String result = queryTransformer.transform(query).text();
        logger.info("Transformed message to: {}", result);
        return result;
    }
}
