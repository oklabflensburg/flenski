package com.flenski.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.flenski.repository")
@EntityScan(basePackages = "com.flenski.entity")
public class JpaConfig {
}