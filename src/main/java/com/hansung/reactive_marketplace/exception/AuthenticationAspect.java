package com.hansung.reactive_marketplace.exception;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class AuthenticationAspect {

    @Before("execution(* com.hansung.reactive_marketplace.controller.*.*(.., org.springframework.security.core.Authentication, ..))")
    private Mono<Authentication> checkAuthentication(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Authentication) {
                Authentication authentication = (Authentication) arg;
                if (authentication.getPrincipal() != null) {
                    return Mono.just(authentication);
                }
                break;
            }
        }
        return Mono.error(new ApiException(ExceptionMessage.UNAUTHORIZED));
    }
}
