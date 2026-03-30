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

> 注記: 前章で `spring-boot-starter-security` 対応として `SecurityConfig` を追加し、`/health` だけ `permitAll` にしている場合、この章で作る `/api/projects` は `401` になることがあります。
> 疎通確認を優先するなら、`SecurityConfig` の `permitAll` に `/api/projects/**` も追加してください。

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/health", "/api/projects/**").permitAll()
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

---

## 2.8 クエリ追加例（`findByName(...)`）

Spring Data JPAでは、`Repository` のメソッド名からクエリを自動生成できます。

### 1) Repositoryにメソッドを追加

`src/main/java/com/example/demo/repository/ProjectRepository.java`

```java
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByName(String name);
}
```

### 2) Controllerから呼び出す

`src/main/java/com/example/demo/api/ProjectApiController.java`

```java
// 省略
import org.springframework.web.bind.annotation.RequestParam;

// 省略
@GetMapping("/search")
public List<Project> searchByName(@RequestParam String name) {
    return repository.findByName(name);
}
```

### 3) 呼び出し例

```bash
curl.exe -i "http://localhost:8080/api/projects/search?name=Sample%20API"
```

補足:
- `findByNameContaining(String keyword)` で部分一致検索
- `findByNameIgnoreCase(String name)` で大文字小文字を無視した検索

---

## 2.9 `JpaRepository` の標準機能（補足）

`ProjectRepository extends JpaRepository<Project, Long>` と定義すると、実装を書かなくても次の代表的なメソッドが使えます。

- `findAll()` : 全件取得
- `findById(id)` : 主キーで1件取得（`Optional`）
- `save(entity)` : 新規作成/更新
- `saveAll(entities)` : 複数保存
- `deleteById(id)` : 主キーで削除
- `delete(entity)` / `deleteAll()` : 削除系
- `existsById(id)` : 存在確認
- `count()` : 件数取得
- `findAllById(ids)` : 複数IDで取得
- `flush()` / `saveAndFlush()` : 即時反映系

実務でまず使う最小セット:

- `findAll`
- `findById`
- `save`
- `deleteById`

注意:
- これらはRepositoryの機能であり、外部APIとして自動公開はされません。
- ユーザー（フロント）から利用するには、`Controller` に `GET/POST/PUT/DELETE` のエンドポイントを定義する必要があります。

---

## 2.10 `save()` 更新時のトランザクションと排他（補足）

既存IDを持つEntityを `save()` した場合、更新処理として扱われます。  
そのときの実務上のポイントは次の2点です。

### 1) 複数テーブル更新のトランザクション

- `save()` 単体より、Serviceメソッドに `@Transactional` を付けて複数更新をまとめるのが基本です。
- 同一トランザクション内で例外が発生した場合は、まとめてロールバックされます。

### 2) 排他（楽観的排他）

- 何も設定しない場合、最後に保存した更新が有効になります。
- 楽観的排他を使うには、Entityに `@Version` フィールドを追加します。

`src/main/java/com/example/demo/model/Project.java`（例）

```java
@Version
private Long version;
```

これにより更新時にバージョン整合性が検証され、競合時は `OptimisticLockException` 系例外が発生します。  
APIではこの競合を `409 Conflict` として返す設計がよく使われます。
