package com.flenski.indexer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(
  // remove this to activate configuration for Postgres
  exclude = { org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class }
)
@ComponentScan(basePackages = "com.flenski")
public class IndexerApplication {

	public static void main(String[] args) {
		SpringApplication.run(IndexerApplication.class, args);
	}
}
