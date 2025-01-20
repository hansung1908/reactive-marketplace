package com.hansung.reactive_marketplace.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("Redis 연결 및 CRUD 작업 테스트")
    void testRedisCRUDOperations() {
        // 테스트 키와 값 정의
        String testKey = "testKey";
        String testValue = "testValue";

        // 데이터 저장
        redisTemplate.opsForValue().set(testKey, testValue);

        // 데이터 조회
        String retrievedValue = redisTemplate.opsForValue().get(testKey);

        // 검증
        assertNotNull(retrievedValue);
        assertEquals(testValue, retrievedValue);

        // 데이터 삭제
        Boolean deleteResult = redisTemplate.delete(testKey);

        // 삭제 검증
        assertTrue(deleteResult);
        assertNull(redisTemplate.opsForValue().get(testKey));
    }

    @Test
    @DisplayName("Redis 리스트 작업 테스트")
    void testRedisListOperations() {
        String listKey = "testList";

        // 리스트에 데이터 추가
        redisTemplate.opsForList().rightPush(listKey, "item1");
        redisTemplate.opsForList().rightPush(listKey, "item2");

        // 리스트 크기 확인
        Long size = redisTemplate.opsForList().size(listKey);
        assertEquals(2, size);

        // 리스트에서 데이터 조회
        String firstItem = redisTemplate.opsForList().index(listKey, 0);
        assertEquals("item1", firstItem);

        // 리스트 삭제
        redisTemplate.delete(listKey);
    }
}

