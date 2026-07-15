package com.zhixun.item.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final Path imageDirectory;
    private final String publicPath;

    public StaticResourceConfig(
            @Value("${zhixun.storage.item-images-path}") String imageDirectory,
            @Value("${zhixun.storage.item-images-public-path}") String publicPath
    ) {
        this.imageDirectory = Path.of(imageDirectory).toAbsolutePath().normalize();
        this.publicPath = publicPath;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(publicPath + "**")
                .addResourceLocations(imageDirectory.toUri().toString());
    }
}
