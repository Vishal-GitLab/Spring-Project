package com.fitness.aiservice.service;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service

// ye class gemini se bat krne ke liye use karenge
public class GeminiService {
  private final WebClient webClient;   // is instance ki help se jo meri gemini ki api hai usko call karenge aur jo activity ka data hai usko denge  fir jo data aayega usko ham data base me save karenge

        // jo yml file hai uska use karenge ham
        @Value("${gemini.api.url}")
       public  String geminiApiUrl;
       @Value("${gemini.api.key}")
       public  String geminiApiKey;

       public GeminiService(WebClient.Builder  webClientBuilder) {
           this.webClient  = webClientBuilder.build();
       }
       public String getRecommendations(String details) {
           Map<String, Object> requestBody =  Map.of(
                   "contents", new Object[] {
                           Map.of("parts", new Object[]  {
                                   Map.of("text",details)
                                   }
                                   )
                   }
           );

           // ham api ko call karenge
           String response = webClient.post()
                   .uri(geminiApiUrl)
                   .header("Content-Type", "application/json")
                           .header("X-goog-api-key", geminiApiKey)
                           .bodyValue(requestBody)
                   .retrieve()
                   .bodyToMono(String.class)
                   .block();
           return response;
       }
}
