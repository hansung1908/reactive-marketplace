package com.hansung.reactive_marketplace.util;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

// 이미지 이름과 관련된 메소드를 모아놓은 유틸리티 클래스
@Component
public class ImageUtils {

    // 중복 방지를 위한 'uuid_이미지명' 형식으로 파일 이름 생성
    public String generateUniqueImageName(String originalFileName) {
        return UUID.randomUUID() + "_" + originalFileName;
    }

    // 이미지 업로드 경로를 생성
    public String generateImagePath(String uploadPath, String fileName) {
        return uploadPath + File.separator + fileName;
    }

    // 이미지 구별을 위해 붙혀둔 uuid를 제거한 원래 이름 추출
    public String extractOriginalName(String fileName) {
        return fileName.substring(fileName.indexOf('_') + 1);
    }

    // url 입력시 한글 깨짐 방지를 위한 파일 이름을 utf-8로 인코딩
    public String encodeImageNameForUrl(String fileName) throws UnsupportedEncodingException {
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
