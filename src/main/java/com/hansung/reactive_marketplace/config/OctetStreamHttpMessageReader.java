package com.hansung.reactive_marketplace.config;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class OctetStreamHttpMessageReader implements HttpMessageReader<MultiValueMap<String, String>> {

    @Override
    public List<MediaType> getReadableMediaTypes() {
        return Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    public boolean canRead(ResolvableType elementType, MediaType mediaType) {
        return true;
    }

    @Override
    public Mono<MultiValueMap<String, String>> readMono(ResolvableType elementType, ReactiveHttpInputMessage message, Map<String, Object> hints) {
        return message.getBody()
                .collectList()  // DataBuffer들을 모두 모은 후
                .map(buffers -> {
                    // DataBuffer들을 하나의 문자열로 변환
                    String body = buffers.stream()
                            .map(buffer -> {
                                byte[] bytes = new byte[buffer.readableByteCount()];
                                buffer.read(bytes);
                                return new String(bytes, StandardCharsets.UTF_8);  // UTF-8로 변환
                            })
                            .reduce("", (s1, s2) -> s1 + s2);  // DataBuffer들 합침

                    // URL 디코딩을 통해 폼 데이터를 파싱
                    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                    String[] keyValuePairs = body.split("&");

                    for (String keyValue : keyValuePairs) {
                        String[] pair = keyValue.split("=");
                        if (pair.length == 2) {
                            String key = UriUtils.decode(pair[0], StandardCharsets.UTF_8);
                            String value = UriUtils.decode(pair[1], StandardCharsets.UTF_8);
                            formData.add(key, value);
                        }
                    }

                    return formData;
                });
    }

    @Override
    public Flux<MultiValueMap<String, String>> read(ResolvableType elementType, ReactiveHttpInputMessage message, Map<String, Object> hints) {
        return message.getBody()  // Flux<DataBuffer>로 스트리밍 데이터를 받음
                .map(buffer -> {
                    // DataBuffer를 바이트 배열로 변환 (UTF-8)
                    byte[] byteArray = new byte[buffer.readableByteCount()];
                    buffer.read(byteArray);  // DataBuffer의 데이터를 바이트 배열로 읽음
                    String body = new String(byteArray, StandardCharsets.UTF_8);  // 바이트 배열을 문자열로 변환

                    // URL 디코딩을 통해 폼 데이터를 파싱
                    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                    String[] keyValuePairs = body.split("&");

                    for (String keyValue : keyValuePairs) {
                        String[] pair = keyValue.split("=");
                        if (pair.length == 2) {
                            String key = UriUtils.decode(pair[0], StandardCharsets.UTF_8);
                            String value = UriUtils.decode(pair[1], StandardCharsets.UTF_8);
                            formData.add(key, value);
                        }
                    }
                    return formData;  // 파싱한 MultiValueMap 반환
                });
    }
}