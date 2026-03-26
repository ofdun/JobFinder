package com.ofdun.jobfinder.features.clients.ai.impl;

import com.ofdun.jobfinder.features.clients.ai.AiClient;
import com.ofdun.jobfinder.features.clients.ai.DTO.OllamaRequest;
import com.ofdun.jobfinder.features.clients.ai.DTO.OllamaResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.web.client.RestClient;

import java.util.List;

@AllArgsConstructor
public class OllamaAiClient implements AiClient {
    private final String endpoint;
    private final String model;

    private final RestClient httpClient;

    public OllamaAiClient(String base, String endpoint, String model) {
        this.endpoint = endpoint;
        this.model = model;

        httpClient = RestClient.builder()
            .baseUrl(base)
            .build();
    }

    @Override
    @NonNull
    public List<Double> getEmbedding(String content) {

        var response = httpClient.post()
                .uri(endpoint)
                .body(new OllamaRequest(model, content))
                .retrieve()
                .body(OllamaResponse.class);

        if (response == null || response.embeddings() == null || response.embeddings().isEmpty()) {
            throw new RuntimeException("ai response empty"); // TODO
        }

        return response.embeddings().getFirst();
    }
}
