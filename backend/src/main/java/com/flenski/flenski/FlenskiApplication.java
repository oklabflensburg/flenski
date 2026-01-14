package com.flenski.flenski;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
	exclude = {
		org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreAutoConfiguration.class
	}
)
@ComponentScan(basePackages = "com.flenski")
@EnableJpaRepositories(basePackages = "com.flenski.repository")
@EntityScan(basePackages = "com.flenski.entity")
@EnableScheduling
public class FlenskiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlenskiApplication.class, args);
	}
}
