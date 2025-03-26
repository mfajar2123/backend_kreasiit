package com.fajar.website.fajar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*") // atau "*" untuk mengizinkan semua asal
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");



    }
}
