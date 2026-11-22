package com.fitness.gateway.user;


import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    @LoadBalanced           // jo hamara servise name banta hai eureka me uski help se ham communication karne wale hai
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
    @Bean
    @LoadBalanced
    // ye pura application ko expose kar dega aur hm is api ko kahi bhi use kar skate h
    public WebClient userServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl("http://USER-SERVICE")
                .build();
    }

}
