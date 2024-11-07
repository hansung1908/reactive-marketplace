package com.hansung.reactive_marketplace.dto.request;

import com.hansung.reactive_marketplace.domain.ImageSource;
import lombok.Getter;

@Getter
public class UserSaveReqDto {

    private String username;

    private String nickname;

    private String password;

    private String email;

    private ImageSource imageSource;

    protected UserSaveReqDto() {
    }

    public UserSaveReqDto(String username, String nickname, String password, String email, ImageSource imageSource) {
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.imageSource = imageSource;
    }

    @Override
    public String toString() {
        return "UserSaveReqDto{" +
                "username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", imageSource=" + imageSource +
                '}';
    }
}
