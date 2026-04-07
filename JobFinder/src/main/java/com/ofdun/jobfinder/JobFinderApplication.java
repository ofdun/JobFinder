package com.ofdun.jobfinder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@RequiredArgsConstructor
public class JobFinderApplication {
    static void main(String[] args) {
        SpringApplication.run(JobFinderApplication.class, args);
    }
}
