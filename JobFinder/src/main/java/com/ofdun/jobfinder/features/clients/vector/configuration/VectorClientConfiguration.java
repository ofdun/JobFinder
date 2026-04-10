package com.ofdun.jobfinder.features.clients.vector.configuration;

import com.ofdun.jobfinder.features.clients.vector.impl.BasicVectorClient;
import com.ofdun.jobfinder.features.clients.vector.properties.QdrantClientProperties;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Collections.VectorParams;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(QdrantClientProperties.class)
public class VectorClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(VectorClientConfiguration.class);

    @Bean
    public BasicVectorClient basicVectorClient(QdrantClientProperties properties)
            throws ExecutionException, InterruptedException {
        log.info(
                "Creating Qdrant client for {}:{} (tls={}) collection={} dim={}",
                properties.getBase(),
                properties.getGrpcPort(),
                properties.getUseTls(),
                properties.getCollectionName(),
                properties.getEmbeddingDimension());
        return new BasicVectorClient(qdrantClient(properties), properties.getCollectionName());
    }

    private QdrantClient qdrantClient(QdrantClientProperties properties)
            throws ExecutionException, InterruptedException {
        var grpcClientBuilder =
                QdrantGrpcClient.newBuilder(
                        properties.getBase(), properties.getGrpcPort(), properties.getUseTls());

        var client = new QdrantClient(grpcClientBuilder.build());

        try {
            boolean exists = client.collectionExistsAsync(properties.getCollectionName()).get();
            log.info("Collection '{}' exists: {}", properties.getCollectionName(), exists);
            if (!exists) {
                log.info(
                        "Creating collection '{}' with dim={}...",
                        properties.getCollectionName(),
                        properties.getEmbeddingDimension());
                client.createCollectionAsync(
                                properties.getCollectionName(),
                                defaultVectorParams(properties.getEmbeddingDimension()))
                        .get();
                log.info("Collection '{}' created", properties.getCollectionName());
            }
        } catch (Exception e) {
            log.error("Failed to check/create collection {}", properties.getCollectionName(), e);
            throw e;
        }

        return client;
    }

    private static VectorParams defaultVectorParams(Integer dim) {
        return VectorParams.newBuilder()
                .setSize(dim)
                .setDistance(Collections.Distance.Cosine)
                .build();
    }
}
