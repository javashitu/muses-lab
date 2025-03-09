package com.muses.recommend.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @ClassName MilvusConfig
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/8 15:14
 */
@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "app.milvus")
public class MilvusConfig {

    private String uri;

    private String token;

    @Bean
    public MilvusClientV2 genMilvusClient(){
        return new MilvusClientV2(ConnectConfig.builder()
                .uri(uri)
                .token(token)
                .build());
    }
}
