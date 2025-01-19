package com.hansung.reactive_marketplace.service.messaging;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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
                .doOnCancel(() -> System.out.println("connected cancel : " + topic));
    }


}
