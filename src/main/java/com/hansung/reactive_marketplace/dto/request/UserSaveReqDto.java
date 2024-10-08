package com.hansung.reactive_marketplace.dto.request;

import lombok.Getter;

@Getter
public class UserSaveReqDto {

    private String userId;

    private String nickname;

    private String password;

    private String email;

    protected UserSaveReqDto() {
    }

    public UserSaveReqDto(String userId, String nickname, String password, String email) {
        this.userId = userId;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
    }

    @Override
    public String toString() {
        return "UserSaveReqDto{" +
                "userId='" + userId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
