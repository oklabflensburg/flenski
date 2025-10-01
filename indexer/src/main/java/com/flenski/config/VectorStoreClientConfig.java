package com.flenski.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;


@Configuration
public class VectorStoreClientConfig {

     @Value("${qdrant.host:localhost}")
    private String host;

    @Value("${qdrant.port:6334}")
    private int port;

    @Value("${qdrant.use-tls:false}")
    private boolean useTls;

    @Bean
    public QdrantClient vectorStoreClient() {

        QdrantClient client = new QdrantClient(QdrantGrpcClient.newBuilder(host, port, useTls).build());
        return client;
    }
    
}
