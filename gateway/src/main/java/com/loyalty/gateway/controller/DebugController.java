package com.loyalty.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@Slf4j
public class DebugController {

    @PostMapping("/log")
    public void logFromClient(@RequestBody Map<String, Object> payload) {
        log.info("[CLIENT DEBUG] {}", payload.get("message"));
        if (payload.containsKey("error")) {
            log.error("[CLIENT ERROR] {}", payload.get("error"));
        }
    }
}
