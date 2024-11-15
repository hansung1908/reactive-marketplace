package com.hansung.reactive_marketplace.util;

import com.hansung.reactive_marketplace.domain.Chat;
import com.hansung.reactive_marketplace.repository.ChatRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

// 채팅방 설정과 관련된 메소드를 모아놓은 유틸리티 클래스
@Component
public class ChatUtils {

    private final ChatRepository chatRepository;

    public ChatUtils(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    // 채팅방 생성을 알려주는 더미 채팅 데이터
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
