package com.flenski.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(

  // remove this to activate configuration for Postgres
  exclude = { org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class }
)
public class AssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssistantApplication.class, args);
	}

}
