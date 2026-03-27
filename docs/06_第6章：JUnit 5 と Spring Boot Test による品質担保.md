# 第6章: JUnit 5 と Spring Boot Test（REST API品質担保）

この章では、API開発で最低限必要な自動テストを整えます。

---

## 6.1 依存関係

以下を確認します。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 6.2 Web APIテスト（MockMvc）

`src/test/java/com/example/demo/api/ProjectApiControllerTest.java`

```java
package com.example.demo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtService jwtService;

    @Test
    void 未認証では401になること() throws Exception {
        mockMvc.perform(get("/api/projects"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 有効なBearerトークンなら200になること() throws Exception {
        String token = jwtService.generateToken("demo");
        mockMvc.perform(get("/api/projects").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
}
```

---

## 6.3 Validation異常系テスト

- `name = ""` で `400` になるか
- エラーフォーマットが契約どおりか

この2つは、Vue連携時の不具合を大きく減らします。

---

## 6.4 Repositoryテスト（必要に応じて）

`@DataJpaTest` で、検索条件や制約のテストを行います。  
ビジネス上重要なクエリ（受注検索、期間絞り込みなど）は必ずテスト対象にします。

---

## 6.5 テスト実行手順（CLI）

テストは「書くだけ」でなく、継続的に実行して初めて品質担保になります。

### 1) 全テスト実行

```bash
./mvnw test
```

### 2) クラス単位で実行

```bash
./mvnw -Dtest=ProjectApiControllerTest test
```

### 3) メソッド単位で実行

```bash
./mvnw -Dtest=ProjectApiControllerTest#未認証では401になること test
```

補足:
- PowerShellでも同じコマンドで実行できます。
- CI実行を意識する場合は `./mvnw -B test` も有効です。

---

## 6.6 テスト実行手順（Eclipse IDE）

Eclipse で作業するメンバー向けに、同等の実行方法を併記します。

### 1) プロジェクト全体のテスト実行

- 対象プロジェクトを右クリック
- `Run As` -> `Maven test`

### 2) クラス単位で実行

- テストクラス（例: `ProjectApiControllerTest`）を右クリック
- `Run As` -> `JUnit Test`

### 3) メソッド単位で実行

- テストメソッド上にカーソルを置く（またはメソッド名を右クリック）
- `Run As` -> `JUnit Test`

### 4) 結果確認

- `JUnit` ビューで成功/失敗件数とスタックトレースを確認
- 詳細ログが必要なら `target/surefire-reports` も併用

---

## 6.7 失敗時の確認ポイント

- Security設定の変更後にテストが古い期待値のまま
- AOP対象を広げすぎてテスト環境に副作用
- ControllerテストでDB初期データ前提が曖昧

テスト失敗時は、まず `SecurityFilterChain` のマッチャー順と期待ステータスを確認します。

失敗詳細は Surefire レポートを確認します。

- `target/surefire-reports/*.txt`
- `target/surefire-reports/*.xml`

---

## 6.8 この章の到達点

- APIの正常系/異常系を自動検証できる
- Security変更時の回帰を検知できる
- CLI/IDEの両方でテストを継続的に回せる
- フロント連携前に品質を固められる