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
@SpringBootTest
@AutoConfigureMockMvc
class ProjectApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void 未認証では401になること() throws Exception {
        mockMvc.perform(get("/api/projects"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER"})
    void 認証済みなら200になること() throws Exception {
        mockMvc.perform(get("/api/projects"))
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

## 6.5 よくある失敗

- Security設定の変更後にテストが古い期待値のまま
- AOP対象を広げすぎてテスト環境に副作用
- ControllerテストでDB初期データ前提が曖昧

テスト失敗時は、まず `SecurityFilterChain` のマッチャー順と期待ステータスを確認します。

---

## 6.6 この章の到達点

- APIの正常系/異常系を自動検証できる
- Security変更時の回帰を検知できる
- フロント連携前に品質を固められる