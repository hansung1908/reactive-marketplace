package com.hansung.reactive_marketplace.dto.request;

import lombok.Getter;

@Getter
public class UserSaveReqDto {

    private String username;

    private String nickname;

    private String password;

    private String email;

    protected UserSaveReqDto() {
    }

    public UserSaveReqDto(String username, String nickname, String password, String email) {
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
    }

    @Override
    public String toString() {
        return "UserSaveReqDto{" +
                "username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
