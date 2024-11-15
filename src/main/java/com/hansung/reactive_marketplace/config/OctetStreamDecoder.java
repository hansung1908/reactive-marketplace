package com.hansung.reactive_marketplace.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.stereotype.Component;

// APPLICATION_OCTET_STREAM 타입으로 오는 값을 json 형태로 바꿔줄 디코더
@Component
public class OctetStreamDecoder extends AbstractJackson2Decoder {
    protected OctetStreamDecoder(ObjectMapper mapper) {
        super(mapper, MediaType.APPLICATION_OCTET_STREAM);
    }
}