package com.example.demo.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ModelMapper configuration for object mapping between entities and DTOs.
 * Provides a configured ModelMapper instance for use throughout the application.
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Creates and configures a ModelMapper instance with standard settings.
     * 
     * @return configured ModelMapper instance
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        
        // Configure matching strategy to be strict for better mapping accuracy
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(false);
        
        return modelMapper;
    }
}