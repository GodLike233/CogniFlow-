package com.cogniflow.config;

import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArkConfig {

    @Value("${ark.api.key}")
    private String apiKey;

    @Bean
    public ArkService arkService() {
        return ArkService.builder()
                .apiKey(apiKey)
                .build();
    }
}
