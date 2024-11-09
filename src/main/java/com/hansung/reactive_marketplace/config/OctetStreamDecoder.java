package com.hansung.reactive_marketplace.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.stereotype.Component;

@Component
public class OctetStreamDecoder extends AbstractJackson2Decoder {
    protected OctetStreamDecoder(ObjectMapper mapper) {
        super(mapper, MediaType.APPLICATION_OCTET_STREAM);
    }
}