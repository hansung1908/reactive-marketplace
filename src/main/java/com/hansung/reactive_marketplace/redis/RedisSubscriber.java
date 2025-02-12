package com.hansung.reactive_marketplace.redis;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class RedisSubscriber {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RedisSubscriber(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 지정된 토픽을 구독(subscribe)하고 메시지를 처리하는 메소드
    public Flux<ServerSentEvent<String>> subscribeToTopic(String topic) {
        return redisTemplate.listenTo(ChannelTopic.of(topic))
                .map(message -> ServerSentEvent.<String>builder()
                        .event("chat-message")
                        .data(message.getMessage())
                        .build())
                .timeout(Duration.ofHours(1))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(300)) // back off 재시도 전략
                        .maxBackoff(Duration.ofSeconds(2)) // 최대 back off 대기 시간
                        .jitter(0.5)) // 50% jitter로 무작위성 추가
                .onErrorResume(error -> Flux.empty()); // 에러 발생시 빈 flux 반환으로 연결 유지
    }


}
