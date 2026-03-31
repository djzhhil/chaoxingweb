package com.chaoxingweb.chaoxing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 超星模块配置
 *
 * @author 小克 🐕💎
 * @since 2026-03-31
 */
@Configuration
public class ChaoxingConfig {

    /**
     * 创建 RestTemplate Bean
     *
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        // 使用 OkHttp3 作为 HTTP 客户端
        OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(10));
        factory.setWriteTimeout(Duration.ofSeconds(10));

        return new RestTemplate(factory);
    }
}
