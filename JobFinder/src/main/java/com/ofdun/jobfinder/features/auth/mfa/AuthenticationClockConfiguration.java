package com.ofdun.jobfinder.features.auth.mfa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AuthenticationClockConfiguration {
    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock authenticationClock() {
        return Clock.systemUTC();
    }
}
