package com.proconsi.electrobazar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("User-Agent", "Electrobazar-TPV/1.0 (Java Client)");
            return execution.execute(request, body);
        });
        return rt;
    }
}
