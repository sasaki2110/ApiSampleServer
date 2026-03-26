# 第3章: REST設計（DTO / Validation / 例外ハンドリング）

この章では、APIを実運用向けに整えます。  
ポイントは「Entityをそのまま返さない」「入力を検証する」「エラーを標準化する」です。

---

## 3.1 DTOを導入する

### Request DTO

`src/main/java/com/example/demo/api/project/ProjectCreateRequest.java`

```java
package com.example.demo.api.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequest(
    @NotBlank
    @Size(max = 100)
    String name,
    @Size(max = 500)
    String description
) {}
```

### Response DTO

`src/main/java/com/example/demo/api/project/ProjectResponse.java`

```java
package com.example.demo.api.project;

public record ProjectResponse(
    Long id,
    String name,
    String description
) {}
```

---

## 3.2 ControllerをDTO中心へ変更

```java
@PostMapping
public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest req) {
    var project = new Project();
    project.setName(req.name());
    project.setDescription(req.description());
    var saved = repository.save(project);
    var body = new ProjectResponse(saved.getId(), saved.getName(), saved.getDescription());
    return ResponseEntity.status(HttpStatus.CREATED).body(body);
}
```

この段階で、フロントはJSON契約だけを見れば良くなります。

---

## 3.3 エラーレスポンスを標準化する

`@RestControllerAdvice` を作って、Validationエラーを同じ形で返します。

例:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "入力値に誤りがあります",
  "details": [
    { "field": "name", "reason": "must not be blank" }
  ]
}
```

---

## 3.4 `spring-boot-starter-validation` を確認

`@Valid` を使うため、依存関係が未追加なら `pom.xml` に入れます。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 3.5 動作確認（curl例）

```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name":"","description":"x"}'
```

期待:
- 400
- 標準化されたValidationエラーJSON

---

## 3.6 この章の到達点

- API契約が明確になった
- 不正入力をサーバで確実に防げるようになった
- Vue側でのエラー表示実装がやりやすくなった

次章では、REST API向けのSecurity設定に進みます。