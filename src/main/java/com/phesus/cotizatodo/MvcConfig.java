package com.phesus.cotizatodo;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 29/01/15
 * Time: 20:28
 */
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //registry.addViewController("/home").setViewName("home");
        registry.addViewController("/").setViewName("home");
        //registry.addViewController("/hello").setViewName("hello");
        //registry.addViewController("/login").setViewName("login");
        //registry.addViewController("/403").setViewName("403");
        //registry.addViewController("/404").setViewName("404");
        //registry.addViewController("/error").setViewName("error");
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource result
         = new ResourceBundleMessageSource();

        String[] basenames = {
         "i18n.messages"
        };

        result.setBasenames(basenames);

        return result;

    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {

        LocaleChangeInterceptor result = new LocaleChangeInterceptor();
        result.setParamName("lang");

        return result;
    }

    @Bean
    public LocaleResolver localeResolver() {

        SessionLocaleResolver result = new SessionLocaleResolver();
        result.setDefaultLocale(Locale.ENGLISH);

        return result;

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
     registry.addInterceptor(localeChangeInterceptor());
    }

}

