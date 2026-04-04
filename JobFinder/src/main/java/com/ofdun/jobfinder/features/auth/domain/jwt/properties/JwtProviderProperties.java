package com.ofdun.jobfinder.features.auth.domain.jwt.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
@Data
public class JwtProviderProperties {
    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-expiration}")
    private Long accessExpirationTimeMs;

    @Value("${app.jwt.refresh-expiration}")
    private Long refreshExpirationTimeMs;
}
