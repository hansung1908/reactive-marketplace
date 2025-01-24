package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import com.hansung.reactive_marketplace.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
public class UserServicePerformanceTest {
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ImageService imageService;
    @Autowired
    private UserService userService;

    private void runPerformanceTest(String testName, int totalRequests, int batchSize,
                                    Mono<?> operation) throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger activeRequests = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int batch = 0; batch < totalRequests/batchSize; batch++) {
            CountDownLatch batchLatch = new CountDownLatch(batchSize);

            for (int i = 0; i < batchSize; i++) {
                long requestStartTime = System.nanoTime();
                activeRequests.incrementAndGet();

                operation
                        .subscribeOn(Schedulers.boundedElastic())
                        .doFinally(signalType -> {
                            long requestEndTime = System.nanoTime();
                            responseTimes.add((requestEndTime - requestStartTime) / 1_000_000);
                            activeRequests.decrementAndGet();
                            batchLatch.countDown();
                        })
                        .doOnSuccess(result -> successCount.incrementAndGet())
                        .doOnError(error -> {
                            errorCount.incrementAndGet();
                            System.err.println("Error in " + testName + ": " + error.getMessage());
                        })
                        .subscribe();
            }

            boolean completed = batchLatch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                System.out.println("Batch " + batch + " did not complete within timeout");
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        printResults(startTime, endTime, successCount.get(), errorCount.get(), responseTimes);
    }

    private void printResults(long startTime, long endTime, int successCount,
                              int errorCount, List<Long> responseTimes) {
        double totalTimeSeconds = (endTime - startTime) / 1000.0;
        double throughput = successCount / totalTimeSeconds;

        System.out.println("Total time: " + (endTime - startTime) + "ms");
        System.out.println("Successful requests: " + successCount);
        System.out.println("Failed requests: " + errorCount);
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
        System.out.println("Average response time: " +
                String.format("%.2f", responseTimes.stream().mapToDouble(Long::doubleValue)
                        .average().orElse(0)) + "ms");
        System.out.println("Min response time: " +
                responseTimes.stream().mapToLong(Long::longValue).min().orElse(0) + "ms");
        System.out.println("Max response time: " +
                responseTimes.stream().mapToLong(Long::longValue).max().orElse(0) + "ms");
    }

    @Test
    public void testSaveUserPerformance() throws InterruptedException {
        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
        when(userRepository.save(any())).thenReturn(
                Mono.just(new User.Builder()
                        .username("testUser")
                        .nickname("Test User")
                        .password("encodedPassword")
                        .email("test@example.com")
                        .build())
        );

        UserSaveReqDto userSaveReqDto = new UserSaveReqDto(
                "testUser", "Test User", "password123", "test@example.com", ImageSource.PROFILE
        );

        runPerformanceTest("SaveUser", 10000, 100,
                userService.saveUser(userSaveReqDto, null));
    }

    @Test
    public void testUpdateUserPerformance() throws InterruptedException {
        when(userRepository.findById(anyString())).thenReturn(
                Mono.just(new User.Builder()
                        .username("testUser")
                        .nickname("Test User")
                        .password("encodedPassword")
                        .email("test@example.com")
                        .build())
        );
        when(userRepository.updateUser(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        UserUpdateReqDto userUpdateReqDto = new UserUpdateReqDto(
                "testUser", "Test User", "password123", "test@example.com", ImageSource.PROFILE
        );

        runPerformanceTest("UpdateUser", 10000, 100,
                userService.updateUser(userUpdateReqDto, null));
    }

    @Test
    public void testFindUserByIdPerformance() throws InterruptedException {
        when(userRepository.findById(anyString())).thenReturn(
                Mono.just(new User.Builder()
                                .username("testUser")
                                .nickname("Test User")
                                .password("encodedPassword")
                                .email("test@example.com")
                                .build())
                        .delayElement(Duration.ofMillis(50)) // 실제 DB 지연 시뮬레이션
        );

        runPerformanceTest("FindUserById", 10000, 100,
                userService.findUserById("testId"));
    }
}


