package fr.nassime.nimbus;

import fr.nassime.nimbus.annotations.type.Get;
import fr.nassime.nimbus.annotations.request.PathVariable;
import fr.nassime.nimbus.annotations.type.Post;
import fr.nassime.nimbus.annotations.request.RequestBody;
import fr.nassime.nimbus.annotations.Controller;
import fr.nassime.nimbus.http.ResponseEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationTest {

    private NimbusServer server;
    private static final int TEST_PORT = 8091;

    @BeforeEach
    void setUp() throws IOException {
        server = NimbusServer.builder()
                .port(TEST_PORT)
                .build();
        server.registerController(new UserController());
        server.start();
        try {
            TimeUnit.MILLISECONDS.sleep(500); // Petit délai pour laisser le serveur démarrer
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldGetUserById() throws IOException {
        // When
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/api/users/123").openConnection();
        connection.setRequestMethod("GET");

        // Then
        assertThat(connection.getResponseCode()).isEqualTo(200);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String response = reader.readLine();
            assertThat(response).contains("\"id\":\"123\"");
            assertThat(response).contains("\"name\":\"User 123\"");
        }
    }

    @Test
    void shouldCreateUser() throws IOException {
        // Given
        String userJson = "{\"name\":\"New User\",\"email\":\"new@example.com\"}";

        // When
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/api/users").openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(userJson.getBytes(StandardCharsets.UTF_8));
        }

        // Then
        assertThat(connection.getResponseCode()).isEqualTo(201);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String response = reader.readLine();
            assertThat(response).contains("\"name\":\"New User\"");
            assertThat(response).contains("\"email\":\"new@example.com\"");
        }
    }

    @Controller(path = "/api/users")
    static class UserController {

        private final Map<String, User> users = new HashMap<>();

        @Get(path = "/{id}")
        public ResponseEntity<User> getUser(@PathVariable("id") String id) {
            User user = new User(id, "User " + id, "user" + id + "@example.com");
            return ResponseEntity.ok(user);
        }

        @Post
        public ResponseEntity<User> createUser(@RequestBody User user) {
            user.setId(String.valueOf(System.currentTimeMillis()));
            users.put(user.getId(), user);
            return ResponseEntity.created(user);
        }
    }

    static class User {
        private String id;
        private String name;
        private String email;

        public User() {
        }

        public User(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
