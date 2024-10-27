package com.hansung.reactive_marketplace.util;

import com.hansung.reactive_marketplace.domain.Chat;
import com.hansung.reactive_marketplace.repository.ChatRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ChatUtils {

    private final ChatRepository chatRepository;

    public ChatUtils(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Mono<Chat> saveDummyChat(String roomId) {
        Chat chat = new Chat.Builder()
                .roomId(roomId)
                .sender("system")
                .receiver("system")
                .msg("대화가 시작되었습니다.")
                .build();

        return chatRepository.save(chat);
    }
}
