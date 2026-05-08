package com.example.noteapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class ServerInfoController {

    /**
     * GET /api/server-info
     * 현재 요청을 처리한 서버 IP·호스트명 반환
     * FE에서 10초마다 호출해 로드밸런싱 전환을 시각화
     */
    @GetMapping("/api/server-info")
    public ResponseEntity<Map<String, Object>> serverInfo() {
        String ip       = "unknown";
        String hostname = "unknown";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            ip       = addr.getHostAddress();
            hostname = addr.getHostName();
        } catch (Exception ignored) {}

        // LinkedHashMap으로 순서 보장
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("serverIp",   ip);
        result.put("hostname",   hostname);
        result.put("timestamp",  LocalDateTime.now().toString());
        result.put("message",    "풀스택 HA Lab — Spring Boot 정상 응답");

        return ResponseEntity.ok(result);
    }
}
