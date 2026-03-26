# 第4章: Spring Security（REST API向け / 方式B: JWT）

この章では、方式B（JWT）でAPIを保護します。  
最終的にSupabase認証へ差し替える前提で、まずはローカルのモック認証で動作確認できる状態を作ります。

---

## 4.1 この章の方針

- 方式B（JWT）を採用する
- まずは `/api/auth/login` でモックJWTを発行する
- `/api/**` はBearerトークン必須にする
- 将来は「トークン発行部分だけ」Supabaseに置換する

---

## 4.2 依存関係を追加する

`pom.xml` の `<dependencies>` に `spring-boot-starter-security` を追加します。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

JWTを扱うため、JJWTも追加します。

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

---

## 4.3 JWTユーティリティを作る（モック）

`src/main/java/com/example/demo/security/JwtService.java`

役割:
- JWTの生成（`generateToken`）
- JWTの検証（`validateToken`）
- ユーザー名の取得（`extractSubject`）

注記:
- 学習用なのでシークレットは一旦固定文字列でも可
- 実運用では `application.yml` / 環境変数で管理する

動く最小例:

```java
package com.example.demo.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // 学習用の固定鍵。実運用では環境変数などで管理すること
    private static final String SECRET =
        "replace-this-with-a-very-long-secret-key-for-hs256-demo-1234567890";

    private static final long EXPIRE_SECONDS = 60 * 60; // 1時間

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public String generateToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(EXPIRE_SECONDS)))
            .signWith(key)
            .compact();
    }

    public String extractSubject(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith((javax.crypto.SecretKey) key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
```

---

## 4.4 モックログインAPIを作る

`src/main/java/com/example/demo/api/auth/AuthController.java`

例:
- `POST /api/auth/login`
- 入力された `username/password` を簡易チェック
- 正しければJWTを返却

返却イメージ:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9....",
  "tokenType": "Bearer"
}
```

動く最小例:

```java
package com.example.demo.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.security.JwtService;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        // 学習用モック: 固定ユーザーのみ受け付ける
        if (!"demo".equals(req.username()) || !"password".equals(req.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtService.generateToken(req.username());
        return ResponseEntity.ok(new LoginResponse(token, "Bearer"));
    }

    public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
    ) {}

    public record LoginResponse(
        String accessToken,
        String tokenType
    ) {}
}
```

---

## 4.5 JWTフィルタを作る

`src/main/java/com/example/demo/security/JwtAuthenticationFilter.java`

処理の流れ:
1. `Authorization` ヘッダを読む
2. `Bearer ` で始まるか確認
3. トークン検証
4. 正しければ `SecurityContext` に認証情報をセット

動く最小例:

```java
package com.example.demo.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.extractSubject(token);

        var authentication = new UsernamePasswordAuthenticationToken(
            username,
            null,
            Collections.emptyList()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
```

注記:
- この段階は学習用のため、権限（authorities）は `emptyList` で最小構成にしています。
- 次の段階で `role` claim を読むようにすれば、`@PreAuthorize` へ発展できます。

---

## 4.6 Security設定をJWT向けに変更する

`src/main/java/com/example/demo/config/SecurityConfig.java`

ポイント:
- `/api/auth/**`, `/h2-console/**`, `/api/health` は公開
- それ以外の `/api/**` は認証必須
- セッションを使わない（`STATELESS`）
- `httpBasic` / `formLogin` は使わない
- `JwtAuthenticationFilter` を `UsernamePasswordAuthenticationFilter` の前に入れる

```java
http
    // JWT（Bearer）前提のためCSRFは無効化（Cookie/セッション運用しない）
    .csrf(csrf -> csrf.disable())
    .cors(withDefaults())
    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    // 未認証（トークンなし等）は 401 を返す
    .exceptionHandling(e -> e.authenticationEntryPoint(
        new org.springframework.security.web.authentication.HttpStatusEntryPoint(
            org.springframework.http.HttpStatus.UNAUTHORIZED
        )
    ))
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/h2-console/**", "/api/health", "/api/auth/**").permitAll()
        .requestMatchers("/api/**").authenticated()
        .anyRequest().denyAll()
    )
    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
    .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
```

`SecurityConfig.java` の完全例:

```java
package com.example.demo.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // JWT（Bearer）前提のためCSRFは無効化（Cookie/セッション運用しない）
            .csrf(csrf -> csrf.disable())
            .cors(withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 未認証（トークンなし等）は 401 を返す
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**", "/api/health", "/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

補足:
- いまはJWTを自前検証するため、`oauth2ResourceServer().jwt()` は使っていません。
- Supabase連携に切り替える段階で、`oauth2ResourceServer().jwt()` への移行を検討できます。

---

## 4.7 動作確認（curl）

### 1) 未認証アクセスは401

```bash
curl.exe -i "http://localhost:8080/api/projects"
```

### 2) ログインしてトークン取得

```bash
curl.exe -X POST "http://localhost:8080/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"demo\",\"password\":\"password\"}"
```

### 3) Bearer付きでアクセス

```bash
curl.exe -i "http://localhost:8080/api/projects" ^
  -H "Authorization: Bearer <取得したトークン>"
```

期待:
- 未認証: `401`
- 認証済み: `200`

---

## 4.8 Supabaseへ差し替えるとき

差し替え対象は主に2点です。

- `AuthController` のモックログイン処理を削除
- `JwtService` の検証をSupabase JWT検証に置換

この章で作った `SecurityFilterChain` とBearer運用は、そのまま活かせます。

---

## 4.9 この章の到達点

- JWT（方式B）でAPI認証の最小構成を作れた
- フロントからBearerでAPIを呼べるようになった
- Supabase認証へ移行しやすい構造を先に作れた