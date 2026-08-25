package com.robert.portfolio.orders.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class OrderServiceConfiguration {

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }
}
