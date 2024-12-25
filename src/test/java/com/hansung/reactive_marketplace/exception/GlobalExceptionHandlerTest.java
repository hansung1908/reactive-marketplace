package com.hansung.reactive_marketplace.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        exceptionHandler = new GlobalExceptionHandler(objectMapper);
    }

    @Test
    void handleApiException() {
        // Given
        ApiException apiException = new ApiException(ExceptionMessage.USERNAME_ALREADY_EXISTS);
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));

        // When
        Mono<Void> result = exceptionHandler.handle(exchange, apiException);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }

    @Test
    void handleGenericException() {
        // Given
        Exception genericException = new RuntimeException("Generic error");
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));

        // When
        Mono<Void> result = exceptionHandler.handle(exchange, genericException);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }
}
