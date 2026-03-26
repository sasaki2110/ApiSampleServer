# 第4章: Spring Security（REST API向け）

この章では、画面遷移型のログインではなく、API保護を前提に設定します。

---

## 4.1 まず決めること

Vue + Spring REST では、先に認証方式を決めると実装が安定します。

- 方式A: セッション（フォーム/クッキー）
- 方式B: トークン（JWT）

学習用の最短ルートは方式Aです。  
本教材では、まず方式Aを例示します。

---

## 4.2 `SecurityFilterChain` の基本

ポイント:
- `/api/auth/**` や `/h2-console/**` は公開
- `/api/**` は認証必須
- CORSはVueの開発サーバ起点を許可

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/h2-console/**", "/api/auth/**").permitAll()
            .requestMatchers("/api/**").authenticated()
            .anyRequest().denyAll()
        )
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/h2-console/**")
        )
        .cors(withDefaults())
        .httpBasic(withDefaults())
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
    return http.build();
}
```

---

## 4.3 CORS設定（Vue連携）

開発時に `http://localhost:5173`（Vite）などからAPIを呼べるようにします。

```java
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
```

---

## 4.4 API向け確認項目

- 未認証で `/api/projects` にアクセスすると `401` になる
- 認証済みで `200` になる
- Vue開発環境からCORSエラーが出ない
- H2コンソールが必要ならアクセス可能

---

## 4.5 この章の到達点

- API保護の境界が明確になった
- フロント分離構成で詰まりやすいCORSを先に解消できた
- 次章の監査ログ導入に進める状態になった