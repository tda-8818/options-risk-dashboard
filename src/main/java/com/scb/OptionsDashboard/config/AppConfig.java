/**
 * AppConfig.java
 *
 * Spring configuration class that declares shared beans used across
 * the application.
 *
 * WHAT IS A BEAN:
 * A bean is just an object that Spring creates and manages for you.
 * Instead of writing "new RestTemplate()" every time you need one,
 * you declare it here once and Spring injects the same instance
 * wherever it's needed. This is called dependency injection.
 *
 * WHY @Configuration:
 * Tells Spring this class contains bean definitions.
 * Spring scans for this annotation at startup and calls the @Bean
 * methods to create the instances.
 *
 * @author  Elsa
 * @version 1.0
 * @since   2026-03-04
 */
package com.scb.OptionsDashboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    /**
     * RestTemplate bean — Spring's built-in HTTP client.
     *
     * Used by MarketDataService to call Yahoo Finance and
     * by DeepSeekService (later) to call the DeepSeek API.
     *
     * Declared as a bean so one instance is shared across the app
     * rather than creating a new HTTP client on every request.
     *
     * @return a RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * ObjectMapper bean — Jackson's JSON parser and serialiser.
     *
     * Used to parse Yahoo Finance's JSON response in MarketDataService.
     * ObjectMapper is thread-safe and expensive to create, so we
     * declare it as a shared singleton bean.
     *
     * @return an ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}