package com.phesus.cotizatodo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.PermissionEvaluator;

@SpringBootApplication
public class CotizatodoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CotizatodoApplication.class, args);
    }

    @Bean
    public PermissionEvaluator permissionEvaluator() {
        CotizadoraPermissionEvaluator bean = new CotizadoraPermissionEvaluator();
        return bean;
    }
}
