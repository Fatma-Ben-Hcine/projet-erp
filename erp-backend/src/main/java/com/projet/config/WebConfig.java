package com.projet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String baseDir = System.getProperty("user.dir");
        String photosPath = baseDir 
                            + File.separator + "uploads" 
                            + File.separator + "photos" 
                            + File.separator;
        String uploadsPath = baseDir 
                             + File.separator + "uploads" 
                             + File.separator;

        System.out.println("=== STATIC RESOURCE PATH ===");
        System.out.println("Serving from: " + uploadsPath);
        System.out.println("============================");

        registry.addResourceHandler("/uploads/photos/**")
                .addResourceLocations("file:" + photosPath)
                .setCachePeriod(0)
                .resourceChain(false);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath)
                .setCachePeriod(0)
                .resourceChain(false);
    }
}
