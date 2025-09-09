package com.fkhrayef.motor.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RAGApiConfig {

    @Value("${rag.api.url}")
    private String ragApiUrl;

    @Bean
    public WebClient ragApiClient() {
        return WebClient.builder()
                .baseUrl(ragApiUrl)
                .defaultHeaders(h -> {
                    h.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    h.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .build();
    }
}
