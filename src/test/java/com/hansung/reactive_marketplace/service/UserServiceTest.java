package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Image;
import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(UserService.class) // WebFlux 테스트를 위한 어노테이션
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository; // userRepository Mock

    @MockBean
    private ImageService imageService; // imageService Mock

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder; // Password Encoder Mock

    @Test
    public void saveUser_whenUsernameDoesNotExist_shouldSaveUser() {
        // given
        UserSaveReqDto userSaveReqDto = new UserSaveReqDto("newUsername", "newNickname", "password", "email@example.com", ImageSource.PROFILE);
        FilePart imagefile = mock(FilePart.class); // 이미지 파일 Mock

        User user = new User.Builder()
                .username(userSaveReqDto.username())
                .nickname(userSaveReqDto.nickname())
                .password("encodedPassword")
                .email(userSaveReqDto.email())
                .build();

        Image image = new Image.Builder()
                .imageSource(ImageSource.PROFILE)
                .userId("user123")
                .imageName("exampleImage.jpg")
                .imageType("jpg")
                .imageSize(1024L)
                .imagePath("/images/exampleImage.jpg")
                .build();

        // Mocking
        when(userRepository.existsByUsername(userSaveReqDto.username())).thenReturn(Mono.just(false)); // 중복 x
        when(bCryptPasswordEncoder.encode(userSaveReqDto.password())).thenReturn("encodedPassword"); // 비밀번호 암호화
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user)); // 사용자 객체 반환
        when(imageService.uploadImage(imagefile, user.getId(), userSaveReqDto.imageSource())).thenReturn(Mono.just(image)); // 이미지 객체 반환

        // when & then
        StepVerifier.create(userService.saveUser(userSaveReqDto, imagefile))
                .expectNextMatches(savedUser -> savedUser.getUsername().equals("newUsername"))
                .verifyComplete();
    }

    @Test
    public void saveUser_whenUsernameAlreadyExists_shouldReturnError() {
        // given
        UserSaveReqDto userSaveReqDto = new UserSaveReqDto("existingUsername", "nickname", "password", "email@example.com", ImageSource.PROFILE);

        // Mocking: Username이 이미 존재한다고 설정
        when(userRepository.existsByUsername(userSaveReqDto.username())).thenReturn(Mono.just(true));

        // when & then
        StepVerifier.create(userService.saveUser(userSaveReqDto, null)) // 이미지는 null로 설정
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Username already exists"))
                .verify(); // expectErrorMatches를 사용하여 예외를 검증

        // verify
        verify(userRepository, never()).save(any(User.class)); // save 메서드가 호출되지 않았는지 확인
    }
}
