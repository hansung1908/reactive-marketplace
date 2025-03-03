package com.hansung.reactive_marketplace.transaction;

import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.dto.request.UserDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import com.hansung.reactive_marketplace.repository.UserRepository;
import com.hansung.reactive_marketplace.service.ImageService;
import com.hansung.reactive_marketplace.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTransactionTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageService imageService;

    private UserSaveReqDto userSaveReqDto;

    @BeforeAll
    public void setup() {
        // 1. 테스트용 사용자 생성 데이터 준비
        userSaveReqDto = new UserSaveReqDto(
                "transactiontest",
                "testnickname",
                "password",
                "test@example.com",
                ImageSource.PROFILE
        );
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    public void testSaveUserTransaction() {
        // 2. 이미지 서비스가 예외를 던지도록 설정 (실제 구현을 모킹)
        ImageService mockedImageService = Mockito.mock(ImageService.class);
        Mockito.when(mockedImageService.uploadImage(any(), anyString(), eq(ImageSource.PROFILE)))
                .thenReturn(Mono.error(new RuntimeException("Simulated error")));

        // 원본 이미지 서비스를 모킹된 서비스로 교체하는 리플렉션 코드가 필요할 수 있음
        // ReflectionTestUtils.setField(userService, "imageService", mockedImageService);
        FilePart mockImage = Mockito.mock(FilePart.class);

        // 3. 트랜잭션 실행 및 예외 확인
        StepVerifier.create(userService.saveUser(userSaveReqDto, mockImage))
                .expectError(RuntimeException.class)
                .verify();

        // 4. 롤백 확인 - 사용자 정보가 저장되지 않았는지 확인
        StepVerifier.create(userRepository.findByUsername("transactiontest"))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void testUpdateUserTransaction() {
        // 1. 테스트용 사용자 생성 및 저장
        StepVerifier.create(userService.saveUser(userSaveReqDto, null))
                .expectNextCount(1)
                .verifyComplete();

        // 2. 업데이트할 데이터 준비
        UserUpdateReqDto updateReqDto = new UserUpdateReqDto(
                // findByUsername으로 사용자 ID 조회 필요
                userRepository.findByUsername("transactiontest").block().getId(),
                "updatedNickname",
                "newPassword",
                "updated@example.com",
                ImageSource.PROFILE
        );

        // 3. 이미지 서비스가 예외를 던지도록 설정
        ImageService mockedImageService = Mockito.mock(ImageService.class);
        Mockito.when(mockedImageService.findProfileImageById(anyString()))
                .thenReturn(Mono.empty());
        Mockito.when(mockedImageService.uploadImage(any(), anyString(), any(ImageSource.class)))
                .thenReturn(Mono.error(new RuntimeException("Simulated error in update")));

        FilePart mockImage = Mockito.mock(FilePart.class);

        // 4. 트랜잭션 실행 및 예외 확인
        StepVerifier.create(userService.updateUser(updateReqDto, mockImage))
                .expectError(RuntimeException.class)
                .verify();

        // 5. 롤백 확인 - 사용자 정보가 업데이트되지 않았는지 확인
        StepVerifier.create(userRepository.findByUsername("transactiontest"))
                .expectNextMatches(user ->
                        user.getNickname().equals("testnickname") && // 원래 닉네임 유지
                                user.getEmail().equals("test@example.com"))  // 원래 이메일 유지
                .verifyComplete();
    }

    @Test
    public void testDeleteUserTransaction() {
        // 1. 테스트용 사용자 생성 및 저장
        StepVerifier.create(userService.saveUser(userSaveReqDto, null))
                .expectNextCount(1)
                .verifyComplete();

        // 2. 삭제 요청 준비
        String userId = userRepository.findByUsername("transactiontest").block().getId();
        UserDeleteReqDto deleteReqDto = new UserDeleteReqDto(userId);

        // 3. 이미지 서비스가 예외를 던지도록 설정
        ImageService mockedImageService = Mockito.mock(ImageService.class);
        Mockito.when(mockedImageService.findProfileImageById(anyString()))
                .thenReturn(Mono.empty());
        Mockito.when(mockedImageService.deleteProfileImageById(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Simulated error in delete")));

        // 4. 트랜잭션 실행 및 예외 확인
        StepVerifier.create(userService.deleteUser(deleteReqDto))
                .expectError(RuntimeException.class)
                .verify();

        // 5. 롤백 확인 - 사용자가 삭제되지 않았는지 확인
        StepVerifier.create(userRepository.findByUsername("transactiontest"))
                .expectNextCount(1) // 사용자가 여전히 존재함
                .verifyComplete();
    }
}

