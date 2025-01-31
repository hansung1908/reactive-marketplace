package com.hansung.reactive_marketplace.redis;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class ReactiveRedisHandler {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    public ReactiveRedisHandler(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public <T> Mono<T> getValue(String key, Class<T> type) {
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .cast(type);
    }

    public <T> Mono<Boolean> setValue(String key, T value, Duration duration) {
        return reactiveRedisTemplate.opsForValue().set(key, value, duration);
    }

    public Mono<Boolean> deleteValue(String key) {
        return reactiveRedisTemplate.opsForValue().delete(key);
    }

    public <T> Mono<T> getOrFetch(String key, Class<T> type, Mono<T> fetcher, Duration duration) {
        return getValue(key, type)
                .switchIfEmpty(
                        fetcher.flatMap(value ->
                                setValue(key, value, duration)
                                        .thenReturn(value)
                        )
                );
    }
}
