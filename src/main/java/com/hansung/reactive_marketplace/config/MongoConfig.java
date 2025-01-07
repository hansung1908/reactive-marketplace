package com.hansung.reactive_marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@Configuration
@EnableReactiveMongoAuditing(dateTimeProviderRef = "kstDateTimeProvider") // @CreatedDate 같은 에너테이션을 사용하기 위한 Auditing 기능 활성화
public class MongoConfig {
    @Bean
    public DateTimeProvider kstDateTimeProvider() {
        return new KstDateTimeProvider();
    }
}
