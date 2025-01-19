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

    private String username;

    private String nickname;

    private String password;

    private String email;

    private Role role;

    @CreatedDate
    private LocalDateTime createdAt;

    protected User() {
    }

    private User(Builder builder) {
        this.username = builder.username;
        this.nickname = builder.nickname;
        this.password = builder.password;
        this.email = builder.email;
        this.role = Role.USER; // 기본값으로 USER 권한 부여
    }

    public static class Builder {
        private String username;
        private String nickname;
        private String password;
        private String email;

        public Builder username(String username) {
            this.username = username;
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
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
