package com.flenski.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import java.util.List;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
public class SecurityConfig {

    @Value("${spring.cors.allowed-origin}")
    private String corsAllowedOrigin;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    org.springframework.web.cors.CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(corsAllowedOrigin));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
                    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/", "/index.html", "/assets/**", "/favicon.ico", "/static/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> {});
        return http.build();
    }
}
