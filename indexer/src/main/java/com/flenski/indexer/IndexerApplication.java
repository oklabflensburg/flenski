package com.flenski.indexer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(
	exclude = {
		org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreAutoConfiguration.class
	}
)
@ComponentScan(basePackages = "com.flenski")
@EnableJpaRepositories(basePackages = "com.flenski.repository")
@EntityScan(basePackages = "com.flenski.entity")

public class IndexerApplication {

	public static void main(String[] args) {
		SpringApplication.run(IndexerApplication.class, args);
	}
}
