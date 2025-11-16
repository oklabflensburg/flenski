package com.flenski.assistant.queryTransformers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.stereotype.Service;

@Service

public class CompressionTransformer {

    private static final Logger logger = LoggerFactory.getLogger(TranslationTransformer.class);
    private final ChatClient.Builder chatClientBuilder;
    private final ChatClient chatClient;

    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("""
			
            Komprimiere die Anfrage so weit wie möglich, ohne die Bedeutung zu verändern.
            Ziel ist es die Anfrage für eine Vektorensuche zu optimieren, indem unnötige Wörter entfernt werden.
            Verwende so wenige Wörter wie möglich.

			Original Anfrage: {query}
			""");

    public CompressionTransformer(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
        this.chatClient = chatClientBuilder.build();
    }

    public String transform(String message) {
        Query query = new Query(message);
        return transform(query);
    }

    public String transform(Query query) {

        logger.info("CompressTransformer called.");

        var compressedQueryText = this.chatClient.prompt()
                .user(user -> user.text(this.DEFAULT_PROMPT_TEMPLATE.getTemplate())
                .param("query", query.text()))
                .call()
                .content();

        logger.info("Transformed message to: {}", compressedQueryText);
        return compressedQueryText;
    }
}
