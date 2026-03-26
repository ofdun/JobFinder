package com.ofdun.jobfinder.features.auth.domain.jwt.configuration;

import com.ofdun.jobfinder.features.auth.domain.jwt.impl.BasicJwtProvider;
import com.ofdun.jobfinder.features.auth.domain.jwt.properties.JwtProviderProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtProviderConfiguration {

    @Bean
    public BasicJwtProvider basicJwtProvider(JwtProviderProperties jwtProviderProperties) {
        return new BasicJwtProvider(jwtProviderProperties.getSecretKey(),
                jwtProviderProperties.getAccessExpirationTimeMs(),
                jwtProviderProperties.getRefreshExpirationTimeMs());
    }
}
