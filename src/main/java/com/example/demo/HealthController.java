package com.example.demo;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // これがREST APIのコントローラーの宣言
public class HealthController {

    @GetMapping("/health") // これがREST APIのエンドポイントの宣言
    public Map<String, Object> health() {
        // 戻り値がJSONで返される（APIのレスポンス）
        // この例ではMapを使ってJSON形式に変換しているが、DTOを使うことも多い
        return Map.of(
            "status", "UP",
            "timestamp", Instant.now().toString(),
            "service", "spring-revival-project"
        );
    }
}
