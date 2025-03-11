package com.muses.recommend.config;

import com.muses.recommend.common.util.JsonFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName ServerConfig
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/11 15:08
 */
@Slf4j
@Configuration
public class ServerConfig {

    @Bean
    public JsonFormatter genJsonFormatter() {
        return new JsonFormatter();
    }
}
