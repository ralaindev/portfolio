package com.robert.portfolio.inventory.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class InventoryServiceConfiguration {

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }
}
