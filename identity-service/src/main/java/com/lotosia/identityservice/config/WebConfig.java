package com.lotosia.identityservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // This configuration class can be extended to handle HEAD requests for POST endpoints
    // For now, we'll handle HEAD requests by ensuring they return proper HTTP status codes
}
