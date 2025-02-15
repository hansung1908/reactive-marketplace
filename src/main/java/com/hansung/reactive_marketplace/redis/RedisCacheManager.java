package com.hansung.reactive_marketplace.redis;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
public class RedisCacheManager {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    public RedisCacheManager(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
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

    public <T> Flux<T> getListValue(String key, Class<T> type) {
        return reactiveRedisTemplate.opsForList()
                .range(key, 0, -1)
                .cast(type);
    }

    public <T> Mono<Long> setListValue(String key, List<T> values, Duration duration) {
        return reactiveRedisTemplate.opsForList()
                .rightPushAll(key, values.toArray())
                .flatMap(result ->
                        reactiveRedisTemplate.expire(key, duration)
                                .thenReturn(result)
                );
    }

    public <T> Mono<Long> appendToList(String key, T value) {
        return reactiveRedisTemplate.opsForList().rightPush(key, value);
    }

    public <T> Flux<T> getOrFetchList(String key, Class<T> type, Flux<T> fetcher, Duration duration) {
        return getListValue(key, type)
                .switchIfEmpty(
                        fetcher.collectList()
                                .flatMap(values ->
                                        setListValue(key, values, duration)
                                                .thenReturn(values)
                                )
                                .flatMapMany(Flux::fromIterable)
                );
    }

    public Mono<Boolean> deleteList(String key) {
        return reactiveRedisTemplate.opsForList().delete(key);
    }
}
