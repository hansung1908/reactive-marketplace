package com.hansung.reactive_marketplace.service.messaging;

import com.hansung.reactive_marketplace.controller.ChatNotificationController;
import com.hansung.reactive_marketplace.domain.Chat;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class RedisSubscriber {
    private final ReactiveRedisTemplate<String, Chat> redisTemplate;
    private final ChatNotificationController notificationController;

    public RedisSubscriber(ReactiveRedisTemplate<String, Chat> redisTemplate, ChatNotificationController notificationController) {
        this.redisTemplate = redisTemplate;
        this.notificationController = notificationController;
        subscribeToTopic("chat-messages");
    }

    // 지정된 토픽을 구독(subscribe)하고 메시지를 처리하는 메소드
    private void subscribeToTopic(String topic) {
        redisTemplate.listenTo(ChannelTopic.of(topic))
                .map(ReactiveSubscription.Message::getMessage)
                .subscribe(message -> notificationController.sendNotification(message));
    }
}
