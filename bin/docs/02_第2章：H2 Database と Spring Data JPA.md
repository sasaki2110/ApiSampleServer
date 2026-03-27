# 第2章: H2 Database と Spring Data JPA（REST API向け）

この章では、APIが扱うデータをJPAで永続化します。

---

## 2.1 H2を使う理由

- ローカルですぐ起動できる
- 開発中の検証が速い
- H2コンソールでテーブル内容をすぐ確認できる

将来はPostgreSQL/MySQLへ置き換える前提で進めます。

---

## 2.2 エンティティ作成

`src/main/java/com/example/demo/model/Project.java`

```java
package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
}
```

---

## 2.3 Repository作成

`src/main/java/com/example/demo/repository/ProjectRepository.java`

```java
package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
```

---

## 2.4 APIエンドポイント作成（CRUDの入口）

`src/main/java/com/example/demo/api/ProjectApiController.java`

```java
package com.example.demo.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Project;
import com.example.demo.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectApiController {

    private final ProjectRepository repository;

    @GetMapping
    public List<Project> findAll() {
        return repository.findAll();
    }
}
```

注記: この段階では学習優先でEntityを直接返しています。第3章でDTOへ切り替えます。

---

## 2.5 `application.yml` 設定

```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    defer-datasource-initialization: true
    hibernate:
      ddl-auto: update
    show-sql: true
```

`data.sql` で初期データを投入する場合は、`defer-datasource-initialization: true` を入れておくのが安全です。  
（テーブル作成後に `data.sql` が実行されるため、`Table "PROJECT" not found` を避けられます）

### 2.5.1 初期データ（任意）

`src/main/resources/data.sql`

```sql
INSERT INTO project (name, description)
VALUES ('Sample API', 'Spring Boot + H2 initial sample data');

INSERT INTO project (name, description)
VALUES ('Task Tracker', 'Learning project for JPA and REST API');
```

---

## 2.6 動作確認

1. `./mvnw compile`
2. `./mvnw spring-boot:run`
3. `GET /api/projects` がJSON配列で返ることを確認
4. `http://localhost:8080/h2-console` で `PROJECT` テーブルを確認

> 注記: 前章で `spring-boot-starter-security` 対応として `SecurityConfig` を追加し、`/api/health` だけ `permitAll` にしている場合、この章で作る `/api/projects` は `401` になることがあります。
> 疎通確認を優先するなら、`SecurityConfig` の `permitAll` に `/api/projects/**` も追加してください。

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/health", "/api/projects/**").permitAll()
    .anyRequest().authenticated()
);
```

curl例:

```bash
curl http://localhost:8080/api/projects
```

HTTPステータスも確認する場合:

```bash
curl -i http://localhost:8080/api/projects
```

PowerShellでは `curl` が `Invoke-WebRequest` のエイリアスとして動作することがあります。  
その場合は `curl.exe` を使ってください。

```powershell
curl.exe -i "http://localhost:8080/api/projects"
```

期待される結果:

- 初期データなし: `[]`
- `data.sql` を追加した場合: 登録した件数のJSON配列

---

## 2.7 この章の到達点

- API + DBの最小ループを実装できた
- Repository経由で永続化基盤を扱えるようになった
- 次章でDTO/Validation/例外ハンドリングへ進む準備ができた
