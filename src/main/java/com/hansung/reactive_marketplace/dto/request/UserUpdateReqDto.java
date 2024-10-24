package com.hansung.reactive_marketplace.dto.request;

import lombok.Getter;

@Getter
public class UserUpdateReqDto {

    private String id;

    private String nickname;

    private String password;

    private String email;

    protected UserUpdateReqDto() {
    }

    public UserUpdateReqDto(String id, String nickname, String password, String email) {
        this.id = id;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
    }

    @Override
    public String toString() {
        return "UserUpdateReqDto{" +
                "id='" + id + '\'' +
                ", nickname='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
