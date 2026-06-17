package com.example.ue_tracker.controller;

import com.example.ue.proto.UeEventPageResponse;
import com.example.ue_tracker.store.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UeEventControllerTest extends AbstractPostgresTest {

    @Autowired TestRestTemplate rest;

    @Test
    void generateThenReadLatestNormalReturnsTimedProtobuf() throws Exception {
        rest.postForObject("/api/generate?count=200", null, String.class);
        ResponseEntity<byte[]> resp = rest.getForEntity(
                "/api/events/latest?model=NORMAL&page=0&size=50", byte[].class);
        UeEventPageResponse page = UeEventPageResponse.parseFrom(resp.getBody());
        assertFalse(page.getEventsList().isEmpty());
        assertTrue(page.getQueryTimeMs() >= 0);
        assertTrue(page.getTotalElements() > 0);
        assertTrue(page.getTotalPages() >= 1);
    }
}
