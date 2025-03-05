package com.hansung.reactive_marketplace.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.customCodecs().register(new OctetStreamDecoder(new ObjectMapper())); // octet-stream 디코더 추가
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/**") // 해당 요청에 대해서
                .addResourceLocations("file:///C:/image/") // Windows 환경 경로
                .addResourceLocations("file:///home/ubuntu/image/"); // Ubuntu 환경 경로
    }
}
