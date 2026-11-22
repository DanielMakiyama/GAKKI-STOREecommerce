package com.gakki.store.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(IaProperties.class)
public class IaConfig {

    // Cliente HTTP do provedor de IA. O tempo de leitura é longo porque o
    // modelo roda no processador e uma resposta pode levar dezenas de segundos.
    @Bean
    public RestClient iaRestClient(RestClient.Builder construtor, IaProperties propriedades) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(5));
        fabrica.setReadTimeout(propriedades.timeout());

        return construtor
                .baseUrl(propriedades.url())
                .requestFactory(fabrica)
                .build();
    }
}
