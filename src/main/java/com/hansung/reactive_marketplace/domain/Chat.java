package com.hansung.reactive_marketplace.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
public class Chat {

    @Id
    private String id;

    private String msg;

    private String sender;

    private String receiver;

    private String productId;

    @CreatedDate
    private LocalDateTime createdAt;
}
