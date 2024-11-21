package com.hansung.reactive_marketplace.util;

import java.util.UUID;

// 이미지 이름과 관련된 메소드를 모아놓은 유틸리티 클래스
public class ImageUtils {

    // 중복 방지를 위한 'uuid_이미지명' 형식으로 파일 이름 생성
    public static String generateUniqueImageName(String originalFileName) {
        return UUID.randomUUID() + "_" + originalFileName;
    }

    // 이미지 업로드 경로를 생성
    public static String generateImagePath(String uploadPath, String fileName) {
        return uploadPath + "/" + fileName;
    }
}
