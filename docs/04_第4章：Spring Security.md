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

---

## 4.5 JWTフィルタを作る

`src/main/java/com/example/demo/security/JwtAuthenticationFilter.java`

処理の流れ:
1. `Authorization` ヘッダを読む
2. `Bearer ` で始まるか確認
3. トークン検証
4. 正しければ `SecurityContext` に認証情報をセット

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
    .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
    .cors(withDefaults())
    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/h2-console/**", "/api/health", "/api/auth/**").permitAll()
        .requestMatchers("/api/**").authenticated()
        .anyRequest().denyAll()
    )
    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
    .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
```

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