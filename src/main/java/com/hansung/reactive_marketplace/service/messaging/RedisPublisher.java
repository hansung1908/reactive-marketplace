package com.hansung.reactive_marketplace.service.messaging;

import com.hansung.reactive_marketplace.domain.Chat;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RedisPublisher {
    private final ReactiveRedisTemplate<String, Chat> redisTemplate;

    public RedisPublisher(ReactiveRedisTemplate<String, Chat> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 지정된 토픽으로 Chat을 발행(publish)하는 메소드
    public Mono<Long> publish(String topic, Chat chat) {
        return redisTemplate.convertAndSend(topic, chat);
    }
}
