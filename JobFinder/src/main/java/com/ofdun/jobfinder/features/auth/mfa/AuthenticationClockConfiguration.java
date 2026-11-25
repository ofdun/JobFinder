package com.ofdun.jobfinder.features.auth.mfa;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationClockConfiguration {
    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock authenticationClock() {
        return Clock.systemUTC();
    }
}
