package com.hansung.reactive_marketplace.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2) // Order(-1) 에 등록된 DefaultErrorWebExceptionHandler 보다 높은 우선순위를 부여하기 위함
public class GlobalExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        if (ex instanceof ApiException) {
            ApiException apiException = (ApiException) ex;
            ExceptionMessage exceptionMessage = apiException.getException();
            response.setStatusCode(exceptionMessage.getStatusCode());
            return response.writeWith(Mono.just(response.bufferFactory().wrap(
                    createErrorResponse(exceptionMessage.getMessage(), exceptionMessage.getStatusCode().value()))));
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(
                    createErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()))));
        }
    }

    private byte[] createErrorResponse(String message, int status) {
        try {
            ErrorResponse errorResponse = new ErrorResponse(message, status);
            return objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            return new byte[0];
        }
    }

    private record ErrorResponse(
            String message,
            int status) {}
}