package com.hansung.reactive_marketplace.config;

import com.hansung.reactive_marketplace.domain.Chat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Chat> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        // Chat 객체를 JSON으로 직렬화/역직렬화하기 위한 serializer 설정
        Jackson2JsonRedisSerializer<Chat> serializer = new Jackson2JsonRedisSerializer<>(Chat.class);

        // Redis key는 String으로, value는 위에서 정의한 serializer를 사용하도록 설정
        RedisSerializationContext.RedisSerializationContextBuilder<String, Chat> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, Chat> context = builder.value(serializer).build();

        // ReactiveRedisTemplate 생성 및 반환
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
