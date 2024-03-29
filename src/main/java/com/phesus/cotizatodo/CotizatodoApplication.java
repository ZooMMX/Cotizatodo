package com.phesus.cotizatodo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.PermissionEvaluator;

import javax.servlet.MultipartConfigElement;
import java.awt.*;
import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class CotizatodoApplication {

    private static final Logger logger = LoggerFactory.getLogger(CotizatodoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CotizatodoApplication.class, args);

    }

    @Bean
    public PermissionEvaluator permissionEvaluator() {
        CotizadoraPermissionEvaluator bean = new CotizadoraPermissionEvaluator();
        return bean;
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("2MB");
        factory.setMaxRequestSize("2MB");
        return factory.createMultipartConfig();
    }

}
