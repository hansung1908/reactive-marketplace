package com.hansung.reactive_marketplace.service.messaging;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RedisPublisher {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RedisPublisher(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 지정된 토픽으로 Chat을 발행(publish)하는 메소드
    public Mono<Long> publish(String topic, String msg) {
        return redisTemplate.convertAndSend(topic, msg);
    }
}
