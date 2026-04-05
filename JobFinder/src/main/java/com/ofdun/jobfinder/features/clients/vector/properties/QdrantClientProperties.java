package com.ofdun.jobfinder.features.clients.vector.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.qdrant")
@Validated
@Data
public class QdrantClientProperties {
    @NotBlank private String base;

    @NotNull private Integer grpcPort;

    @NotNull private Integer restPort;

    @NotNull private Boolean useTls;

    @NotNull private String collectionName;

    @NotNull private Integer embeddingDimension;
}
