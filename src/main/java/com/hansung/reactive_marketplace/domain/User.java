package com.hansung.reactive_marketplace.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "user")
public class User {

    @Id
    private String id;

    private String userId;

    private String nickname;

    private String password;

    private String email;

    @CreatedDate
    private LocalDateTime created_at;

    protected User() {
    }

    private User(Builder builder) {
        this.userId = builder.userId;
        this.nickname = builder.nickname;
        this.password = builder.password;
        this.email = builder.email;
    }

    public static class Builder {
        private String userId;
        private String nickname;
        private String password;
        private String email;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", created_at=" + created_at +
                '}';
    }
}
