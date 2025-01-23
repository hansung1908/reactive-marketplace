package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.dto.request.UserSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.UserUpdateReqDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.scheduler.Schedulers;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.multipart.FilePart;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class UserServicePerformanceTest {

    @Autowired
    private UserService userService; // 테스트할 서비스 주입

    @Test
    public void testSaveUserPerformance() throws InterruptedException {
        int concurrentRequests = 100000;
        CountDownLatch latch = new CountDownLatch(concurrentRequests);

        UserSaveReqDto userSaveReqDto = new UserSaveReqDto(
                "testUser", "Test User", "password123", "test@example.com", ImageSource.PROFILE
        );
        FilePart mockImage = null; // 실제 테스트에서는 mock FilePart 객체 생성 필요

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < concurrentRequests; i++) {
            final String username = "testUser" + i;
            userService.saveUser(new UserSaveReqDto(username, userSaveReqDto.nickname(),
                            userSaveReqDto.password(), userSaveReqDto.email(),
                            userSaveReqDto.imageSource()), mockImage)
                    .subscribeOn(Schedulers.parallel()) // 해당 처리를 병렬로 진행
                    .subscribe(
                            user -> latch.countDown(),
                            error -> latch.countDown()
                    );
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        System.out.println("All requests completed: " + completed);
        System.out.println("Total time for " + concurrentRequests + " requests: "
                + (endTime - startTime) + " ms");
    }

    @Test
    public void testUpdateUserPerformance() throws InterruptedException {
        int concurrentRequests = 100000;
        CountDownLatch latch = new CountDownLatch(concurrentRequests);

        UserUpdateReqDto userUpdateReqDto = new UserUpdateReqDto(
                "testUser", "Test User", "password123", "test@example.com", ImageSource.PROFILE
        );
        FilePart mockImage = null; // 실제 테스트에서는 mock FilePart 객체 생성 필요

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < concurrentRequests; i++) {
            final String username = "testUser" + i;
            userService.updateUser(new UserUpdateReqDto(username, userUpdateReqDto.nickname(),
                            userUpdateReqDto.password(), userUpdateReqDto.email(),
                            userUpdateReqDto.imageSource()), mockImage)
                    .subscribeOn(Schedulers.parallel()) // 해당 처리를 병렬로 진행
                    .subscribe(
                            user -> latch.countDown(),
                            error -> latch.countDown()
                    );
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        System.out.println("All requests completed: " + completed);
        System.out.println("Total time for " + concurrentRequests + " requests: "
                + (endTime - startTime) + " ms");
    }
}

