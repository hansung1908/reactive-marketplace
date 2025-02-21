package com.hansung.reactive_marketplace.controller;

import com.hansung.reactive_marketplace.dto.response.UserProfileResDto;
import com.hansung.reactive_marketplace.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void loginForm_ShouldReturnCorrectViewName() {
        String viewName = userController.loginForm();
        assertThat(viewName).isEqualTo("user/loginForm");
    }

    @Test
    void saveForm_ShouldReturnCorrectViewName() {
        String viewName = userController.saveForm();
        assertThat(viewName).isEqualTo("user/saveForm");
    }

    @Test
    void profileForm_ShouldReturnCorrectViewName() {
        // Given
        Authentication authentication = mock(Authentication.class);
        UserProfileResDto mockProfile = new UserProfileResDto(
                "testUser",
                "nickname",
                "test@email.com",
                "path/to/image"
        );

        // When
        when(userService.findUserProfile(any(Authentication.class)))
                .thenReturn(Mono.just(mockProfile));

        Mono<Rendering> result = userController.profileForm(authentication);

        // Then
        StepVerifier.create(result)
                .consumeNextWith(rendering -> {
                    assertThat(rendering.view()).isEqualTo("user/profileForm");
                    assertThat(rendering.modelAttributes())
                            .containsKey("profile")
                            .containsValue(mockProfile);
                })
                .verifyComplete();
    }
}


