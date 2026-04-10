package com.ofdun.jobfinder.features.clients.ai.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.ollama")
@Validated
@Data
public class OllamaClientProperties {
    @NotEmpty private String endpoint;

    @NotEmpty @URL private String url;

    @NotEmpty private String model;
}
