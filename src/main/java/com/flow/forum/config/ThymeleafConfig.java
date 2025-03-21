package com.flow.forum.config;

import com.flow.forum.util.ThymeleafDateFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThymeleafConfig {

    @Bean(name = "thymeleafDateFormatter")
    public ThymeleafDateFormatter dateFormatter() {
        return new ThymeleafDateFormatter();
    }
}
