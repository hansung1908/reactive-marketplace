package com.hansung.reactive_marketplace.transaction;

import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.domain.User;
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
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTransactionIntegrationTest {

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
}

