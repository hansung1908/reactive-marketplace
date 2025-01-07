package com.hansung.reactive_marketplace.config;

import org.springframework.data.auditing.DateTimeProvider;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

public class KstDateTimeProvider implements DateTimeProvider {
    @Override
    public Optional<TemporalAccessor> getNow() {
        return Optional.of(LocalDateTime.now().plusHours(9));
    }
}
