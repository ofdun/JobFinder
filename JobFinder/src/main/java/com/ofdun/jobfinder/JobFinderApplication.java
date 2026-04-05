package com.ofdun.jobfinder;

import com.ofdun.jobfinder.features.clients.ai.AiClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@RequiredArgsConstructor
public class JobFinderApplication implements ApplicationRunner {
    private final AiClient aiClient;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        var response = aiClient.getEmbedding("Acoustic regard");

        System.out.println(response);
    }

    public static void main(String[] args) {
        SpringApplication.run(JobFinderApplication.class, args);
    }
}
