package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.dto.request.UserDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import com.hansung.reactive_marketplace.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApiControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserApiController userApiController;

    @Test
    void save_ShouldReturnCreatedStatus() {
        // Given
        UserSaveReqDto saveReqDto = new UserSaveReqDto(
                "testUser",
                "nickname",
                "testPassword",
                "test@email.com",
                ImageSource.PROFILE
        );
        FilePart mockImage = mock(FilePart.class);
        when(userService.saveUser(any(UserSaveReqDto.class), any(FilePart.class)))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userApiController.save(saveReqDto, mockImage))
                .expectNextMatches(response ->
                        response.getStatusCode() == HttpStatus.CREATED &&
                                response.getBody().equals("User saved successfully")
                )
                .verifyComplete();
    }

    @Test
    void update_ShouldReturnOkStatus() {
        // Given
        UserUpdateReqDto updateReqDto = new UserUpdateReqDto(
                "testId",
                "nickname",
                "testPassword",
                "test@email.com",
                ImageSource.PROFILE
        );
        FilePart mockImage = mock(FilePart.class);
        when(userService.updateUser(any(UserUpdateReqDto.class), any(FilePart.class)))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userApiController.update(updateReqDto, mockImage))
                .expectNextMatches(response ->
                        response.getStatusCode() == HttpStatus.OK &&
                                response.getBody().equals("User updated successfully")
                )
                .verifyComplete();
    }

    @Test
    void delete_ShouldReturnOkStatus() {
        // Given
        UserDeleteReqDto deleteReqDto = new UserDeleteReqDto(
                "testId"
        );
        when(userService.deleteUser(any(UserDeleteReqDto.class)))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userApiController.delete(deleteReqDto))
                .expectNextMatches(response ->
                        response.getStatusCode() == HttpStatus.OK &&
                                response.getBody().equals("User deleted successfully")
                )
                .verifyComplete();
    }
}

