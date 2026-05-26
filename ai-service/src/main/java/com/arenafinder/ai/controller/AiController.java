package com.arenafinder.ai.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
@Slf4j
public class AiController {

    @Value("${GROQ_API_KEY:}")
    private String groqApiKey;

    private final WebClient webClient;

    public AiController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.groq.com/openai/v1").build();
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String userMessage = request.getMessage().toLowerCase();
        log.info("Received AI chat request: {}", userMessage);

        // If GROQ key is configured, try calling Groq API
        if (groqApiKey != null && !groqApiKey.trim().isEmpty() && !groqApiKey.equals("${GROQ_API_KEY}")) {
            try {
                ChatResponse response = callGroqApi(request.getMessage()).block();
                if (response != null && response.getResponse() != null) {
                    return ResponseEntity.ok(response);
                }
            } catch (Exception e) {
                log.error("Failed to call Groq API, falling back to local recommendation engine", e);
            }
        }

        // Rule-based Fallback Recommendation Engine
        String aiResponseText = generateSmartFallbackResponse(userMessage);
        return ResponseEntity.ok(new ChatResponse(aiResponseText, true));
    }

    private Mono<ChatResponse> callGroqApi(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama3-8b-8192");
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are the Arena Finder AI Assistant. Help users find and book sports arenas. " +
                "We currently support Varanasi (Champions Turf for Football at 500/hr, City Badminton Hall for Badminton at 300/hr) " +
                "and Mumbai (Sports Arena Mumbai for both Football/Badminton at 800/hr). Be concise, sporty, and helpful.");
        
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        
        messages.add(systemMessage);
        messages.add(userMsg);
        requestBody.put("messages", messages);

        return this.webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + groqApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                        Map<String, Object> firstChoice = choices.get(0);
                        Map<String, String> message = (Map<String, String>) firstChoice.get("message");
                        return new ChatResponse(message.get("content"), false);
                    } catch (Exception e) {
                        log.error("Error parsing Groq response", e);
                        return new ChatResponse(generateSmartFallbackResponse(prompt.toLowerCase()), true);
                    }
                });
    }

    private String generateSmartFallbackResponse(String msg) {
        if (msg.contains("football") || msg.contains("soccer") || msg.contains("turf")) {
            return "⚽ Based on your query, I highly recommend **Champions Turf** in Varanasi (Lanka). It is a top-tier artificial grass turf open from 06:00 AM to 10:00 PM at just **500 INR/hr**. I can help you book it right now!";
        } else if (msg.contains("badminton") || msg.contains("court") || msg.contains("shuttle")) {
            return "🏸 For Badminton, the **City Badminton Hall** in Varanasi (Sigra) is excellent! It has professional indoor wooden courts, open 07:00 AM to 09:00 PM for **300 INR/hr**. Would you like to check available slots?";
        } else if (msg.contains("mumbai") || msg.contains("andheri") || msg.contains("both")) {
            return "🌆 If you're in Mumbai, you must check out **Sports Arena Mumbai** located in Andheri. It supports both Football and Badminton with premium amenities, open from 05:00 AM to 11:00 PM for **800 INR/hr**!";
        } else if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey")) {
            return "👋 Hey there! I am your Arena Finder AI Assistant. Ask me to find the best arenas for Football or Badminton in Varanasi or Mumbai, or ask about pricing and timings!";
        } else {
            return "🤖 I am here to help you find the perfect sports arena! We have fantastic options like:\n\n" +
                    "- **Champions Turf** (Football, Varanasi) — 500 INR/hr\n" +
                    "- **City Badminton Hall** (Badminton, Varanasi) — 300 INR/hr\n" +
                    "- **Sports Arena Mumbai** (Football & Badminton, Mumbai) — 800 INR/hr\n\n" +
                    "Tell me which sport or city you are interested in, and I will find the best match!";
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResponse {
        private String response;
        private boolean fallback;
    }
}
