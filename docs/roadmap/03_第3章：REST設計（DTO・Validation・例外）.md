# 第3章: REST設計（DTO / Validation / 例外ハンドリング）

この章では、APIを実運用向けに整えます。  
ポイントは「Entityをそのまま返さない」「入力を検証する」「エラーを標準化する」です。

---

## 3.1 先にValidation依存関係を追加する

第1章時点で `spring-boot-starter-validation` をまだ追加していない場合は、最初に `pom.xml` を編集します。

`pom.xml` の `<dependencies>` に次を追加:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

追加後は一度 `./mvnw compile` を実行し、依存解決できることを確認してください。

---

## 3.2 DTOを導入する

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

## 3.3 ControllerをDTO中心へ変更

`@PostMapping` だけでなく、`@Valid` / `ResponseEntity` / DTO型を使うために `import` 追加が必要です。

### 追加が必要な主なimport

```java
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.api.project.ProjectCreateRequest;
import com.example.demo.api.project.ProjectResponse;
import com.example.demo.model.Project;
import com.example.demo.repository.ProjectRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
```

### `ProjectApiController` 全体の更新例

```java
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectApiController {

    private final ProjectRepository repository;

    @GetMapping
    public List<ProjectResponse> findAll() {
        return repository.findAll().stream()
            .map(p -> new ProjectResponse(p.getId(), p.getName(), p.getDescription()))
            .toList();
    }

@PostMapping
public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest req) {
    var project = new Project();
    project.setName(req.name());
    project.setDescription(req.description());
    var saved = repository.save(project);
    var body = new ProjectResponse(saved.getId(), saved.getName(), saved.getDescription());
    return ResponseEntity.status(HttpStatus.CREATED).body(body);
}
}
```

この段階で、フロントはJSON契約だけを見れば良くなります。

---

## 3.4 エラーレスポンスを標準化する

ここでは「新規ファイルを2つ作る」のが対象です。

- `src/main/java/com/example/demo/api/error/ApiErrorResponse.java`
- `src/main/java/com/example/demo/api/error/GlobalExceptionHandler.java`

### 1) エラーレスポンスDTOを作る

```java
package com.example.demo.api.error;

import java.util.List;

public record ApiErrorResponse(
    String code,
    String message,
    List<FieldErrorDetail> details
) {
    public record FieldErrorDetail(String field, String reason) {}
}
```

### 2) `@RestControllerAdvice` を作る

```java
package com.example.demo.api.error;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiErrorResponse.FieldErrorDetail> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> new ApiErrorResponse.FieldErrorDetail(err.getField(), err.getDefaultMessage()))
            .toList();

        var body = new ApiErrorResponse(
            "VALIDATION_ERROR",
            "入力値に誤りがあります",
            details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
```

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