package com.example.demo;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * ServletInitializer for WAR deployment configuration.
 * 
 * This class extends SpringBootServletInitializer to support traditional
 * WAR deployment to external servlet containers if needed, while maintaining
 * the primary executable JAR deployment strategy.
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DemoApplication.class);
    }
}