package com.ofdun.jobfinder.features.clients.ai.configuration;

import com.ofdun.jobfinder.features.clients.ai.impl.OllamaAiClient;
import com.ofdun.jobfinder.features.clients.ai.properties.OllamaClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaAiClientConfiguration {

    @Bean
    public OllamaAiClient ollamaAiClient(OllamaClientProperties ollamaClientProperties) {
        return new OllamaAiClient(
                ollamaClientProperties.getUrl(),
                ollamaClientProperties.getEndpoint(),
                ollamaClientProperties.getModel());
    }
}
