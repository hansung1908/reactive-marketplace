package com.hansung.reactive_marketplace.dto.request;

import com.hansung.reactive_marketplace.domain.ImageSource;
import lombok.Getter;

@Getter
public class UserUpdateReqDto {

    private String id;

    private String nickname;

    private String password;

    private String email;

    private ImageSource imageSource;

    protected UserUpdateReqDto() {
    }

    public UserUpdateReqDto(String id, String nickname, String password, String email, ImageSource imageSource) {
        this.id = id;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.imageSource = imageSource;
    }

    @Override
    public String toString() {
        return "UserUpdateReqDto{" +
                "id='" + id + '\'' +
                ", nickname='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", imageSource=" + imageSource +
                '}';
    }
}
