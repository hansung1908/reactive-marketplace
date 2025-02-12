package com.hansung.reactive_marketplace.redis;

import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class RedisPublisher {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RedisPublisher(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 지정된 토픽으로 Chat을 발행(publish)하는 메소드
    public Mono<Long> publish(String topic, String msg) {
        return redisTemplate.convertAndSend(topic, msg)
                .timeout(Duration.ofSeconds(3))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(300)) // back off 재시도 전략
                        .maxBackoff(Duration.ofSeconds(2)) // 최대 back off 대기 시간
                        .jitter(0.5)) // 50% jitter로 무작위성 추가
                .onErrorResume(e -> Mono.error(new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR)));
    }
}
