# 第1章: Spring API基礎・環境構築

この章では、Thymeleafを使わずにREST APIの土台を作ります。

---

## 1.1 プロジェクト作成時の依存関係

Spring Initializr（またはPleiadesのスターター）で、次を選択します。

- `Spring Web`（または `spring-boot-starter-webmvc`）
- `Spring Data JPA`
- `H2 Database`
- `Spring Boot DevTools`
- `Lombok`

`Thymeleaf` は不要です。
`Spring Security` と `Spring Boot Starter Test` は後続の章で導入します。

---

## 1.2 `pom.xml` の最小構成（REST API向け）

既存プロジェクトを整理する場合は、以下を目安にします。

- 残す: Web, JPA, H2, Security, Test, DevTools, Lombok
- 削除: Thymeleaf系依存（`spring-boot-starter-thymeleaf` など）

補足:
- `spring-boot-starter-aop` を使う場合、親が `4.0.4` なら通常は `<version>` を固定せず親に合わせます。

---

## 1.3 最初の疎通APIを作る

`src/main/java/com/example/demo/HealthController.java`

```java
package com.example.demo;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "timestamp", Instant.now().toString(),
            "service", "spring-revival-project"
        );
    }
}
```

---

## 1.4 起動と確認

1. `./mvnw compile`
2. `./mvnw spring-boot:run`
3. `GET http://localhost:8080/health` を呼び、200 + JSON返却を確認

`spring-boot-starter-security` を入れている場合は、デフォルトで認証が有効になり `401` になることがあります。
この章では疎通確認を優先するため、`/health` を無認証で許可します。

`src/main/java/com/example/demo/config/SecurityConfig.java`

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

PowerShell例:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/health"
```

curl例:

```bash
curl http://localhost:8080/health
```

HTTPステータスも確認する場合:

```bash
curl -i http://localhost:8080/health
```

---

## 1.5 この章の到達点

- REST APIプロジェクトとしての最小構成ができた
- コントローラーがHTMLではなくJSONを返す形に切り替わった
- フロント（Vue）と分離して開発する前提が整った

次章では、H2 + JPAで永続化付きAPIを実装します。