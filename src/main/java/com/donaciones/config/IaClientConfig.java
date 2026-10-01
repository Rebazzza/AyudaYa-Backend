package com.donaciones.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class IaClientConfig {

    @Bean
    public RestClient iaRestClient(@Value("${app.ia.base-url}") String baseUrl,
                                   @Value("${app.ia.verificar-dni-path}") String verificarDniPath,
                                   @Value("${app.ia.timeout:PT20S}") Duration timeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        return RestClient.builder()
                .baseUrl(baseUrl + verificarDniPath)
                .requestFactory(requestFactory)
                .build();
    }

}
