package com.flenski.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatClientConfig {

    @Bean("chatClient")
    public ChatClient chatClient(
            ChatClient.Builder chatClientBuilder
    ) {
        ChatOptions chatOptions = MistralAiChatOptions.builder()
                .model(MistralAiApi.ChatModel.LARGE.getValue())
                .temperature(0.5)
                .build();

        return chatClientBuilder
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor())
                )
                .defaultOptions(chatOptions)
                .build();
    }
}
