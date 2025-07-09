<div align="center">
  <img src="images/nimbus_transparant.png" alt="Logo Nimbus" width="300"/>
</div>

![Latest Version](https://img.shields.io/github/v/release/na2sime/Nimbus?include_prereleases&label=Latest%20Version)
![GitHub](https://img.shields.io/github/license/na2sime/Nimbus)
![Java Version](https://img.shields.io/badge/Java-17%2B-blue)

Nimbus is a lightweight, annotation-driven Java microframework for rapidly building RESTful APIs with minimal boilerplate. It provides:

- **Automatic controller scanning** via a simple YAML config 🔍
- **Annotation-based routing** (`@Controller`, `@Get`, `@Post`, `@Put`, `@Delete`) 🛣️
- **Middleware support** through a `Middleware` interface and `@WithMiddleware` 🔄
- **Built-in HTTP server** with configurable thread-pool, port, and verbose logging 🚀

---

## 📑 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Configuration](#configuration)
4. [Bootstrapping Your App](#bootstrapping-your-app)
5. [Defining Controllers](#defining-controllers)
6. [Middleware](#middleware)
7. [Quality & Testing](#quality--testing)
8. [Contributing](#contributing)
9. [License](#license)

---

## ⚙️ Prerequisites

- **Java 17** (or later) ☕
- **Maven** *or* **Gradle** 🛠️
- **Git** 📦

---

## 📥 Installation

### Available Versions

Latest stable version: ![Latest Release](https://img.shields.io/github/v/release/na2sime/Nimbus?label=release)

| Branch  | Status | Description              |
|---------|--------|--------------------------|
| main    | Stable | Current stable version   |
| develop | Dev    | Latest development build |

You can also check all available versions on our [releases page](https://github.com/na2sime/Nimbus/releases).


### Gradle

Add Nimbus to your `build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven {
        name = "Nimbus"
        url = uri("https://maven.pkg.github.com/na2sime/Nimbus")
    }
}

dependencies {
    implementation 'fr.nassime:nimbus:1.0.0'
}
```

### Maven

Add this to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>nimbus</id>
        <url>https://maven.pkg.github.com/na2sime/Nimbus</url>
    </repository>
</repositories>
```

```xml
<dependency>
  <groupId>fr.nassime</groupId>
  <artifactId>nimbus</artifactId>
  <version>1.0.0</version>
</dependency>
```

---

## ⚡ Configuration

Nimbus looks for a `nimbus.yaml` on the classpath root. Example:

```yaml
server:
  port: 8080
  threadPoolSize: 20
  verbose: true

security:
  requireApiKey: true
  apiKeys:
    keys:
      - "sk-123456789"

scanning:
  autoScanControllers: true
  basePackage: "fr.nassime.nimbus.example"
```
This configures port, thread pool size, API-key requirement, and tells Nimbus which package to scan for controllers.

---

## 🚀 Bootstrapping Your App

Create a main class annotated with `@NimbusApp`, then call `NimbusApplication.run(...)`:

```java
@NimbusApp
public class SimpleExample {
    public static void main(String[] args) throws IOException {
        NimbusApplication.run(SimpleExample.class, args);
    }
}
```

This will start the embedded HTTP server and register all your controllers automatically.

---

## 🎮 Defining Controllers

### Controller Example

```java
@Controller(path = "/api/users")
@WithMiddleware(AuthMiddleware.class)
public class UserController {

    @Get(path = "/{id}")
    @WithMiddleware(AdminMiddleware.class)
    public ResponseEntity<User> getUser(@PathVariable("id") String id) { … }

    @Post
    @WithMiddlewares({
        AuthMiddleware.class,
        AdminMiddleware.class,
        RateLimitMiddleware.class
    })
    public ResponseEntity<User> createUser(@RequestBody User user) { … }

    @Put(path = "/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") String id, @RequestBody User user) { … }

    @Delete(path = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") String id) { … }
}
```
Each method returns a `ResponseEntity<T>` to control status codes and bodies.

---

## 🔄 Middleware

Implement the `Middleware` interface to intercept requests:

```java
public class AuthMiddleware implements Middleware {
    @Override
    public boolean handle(HttpExchange exchange) throws IOException {
        // check “Authorization: Bearer <token>” header...
    }
}
```
Register it on a controller or individual method with `@WithMiddleware(AuthMiddleware.class)`.

---

## 🧪 Quality & Testing

Nimbus projects typically include a `pre-commit` hook to enforce:

- Google Java Format ✨
- Checkstyle rules & unused-import checks 📝
- Duplicate-code detection (CPD) 🔍
- YAML/JSON syntax validation ✅
- Merge-conflict and large-file guards ⚔️
- Gradle compile & test runs 🔨

Install with:

```bash
pip install pre-commit
pre-commit install
```

Run all checks manually:

```bash
./quality-check.sh
```

---

## 👥 Contributing

We welcome contributions!

1. Fork the repo 🍴
2. Create a feature branch (`git checkout -b feature/YourFeature`) 🌿
3. Commit with descriptive messages 📝
4. Ensure all pre-commit checks pass ✅
5. Open a Pull Request against `develop` 🎯

---

## 📜 License

This project is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.
