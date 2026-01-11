package com.flenski.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;


@Configuration
public class VectorStoreClientConfig {

    @Value("${spring.ai.vectorstore.qdrant.host}")
    private String host;

    @Value("${spring.ai.vectorstore.qdrant.port}")
    private int port;

    @Value("${spring.ai.vectorstore.qdrant.use_tls:false}")
    private boolean useTls;

    @Value("${spring.ai.vectorstore.qdrant.collection}")
    private String collectionName;

    @Value("${spring.ai.vectorstore.qdrant.api_key}")
    private String apiKey;

    @Bean
    public QdrantClient vectorStoreClient() {

        QdrantClient client = new QdrantClient(QdrantGrpcClient.newBuilder(host, port, useTls).build());
        return client;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getApiKey() { return apiKey; }
}
