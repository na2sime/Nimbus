package fr.nassime.nimbus;

import fr.nassime.nimbus.api.annotations.Get;
import fr.nassime.nimbus.api.annotations.Controller;
import fr.nassime.nimbus.api.http.ResponseEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NimbusServerTest {

    private NimbusServer server;
    private static final int TEST_PORT = 8090;

    @BeforeEach
    void setUp() throws IOException {
        server = NimbusServer.builder()
            .port(TEST_PORT)
            .verbose(true)
            .build();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldStartAndStopServer() {
        assertDoesNotThrow(() -> {
            server.start();
            TimeUnit.MILLISECONDS.sleep(500); // Petit délai pour laisser le serveur démarrer
            server.stop(0);
        });
    }

    @Test
    void shouldHandleGetRequest() throws IOException, InterruptedException {
        // Given
        server.addRoute("/hello", exchange -> {
            String response = "Hello, World!";
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        });
        server.start();
        TimeUnit.MILLISECONDS.sleep(500); // Petit délai pour laisser le serveur démarrer

        // When
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/hello").openConnection();
        connection.setRequestMethod("GET");

        // Then
        assertThat(connection.getResponseCode()).isEqualTo(200);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String response = reader.readLine();
            assertThat(response).isEqualTo("Hello, World!");
        }
    }

    @Test
    void shouldRegisterControllerAndHandleRequest() throws IOException, InterruptedException {
        // Given
        server.registerController(new TestController());
        server.start();
        TimeUnit.MILLISECONDS.sleep(500); // Petit délai pour laisser le serveur démarrer

        // When
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/api/test").openConnection();
        connection.setRequestMethod("GET");

        // Then
        assertThat(connection.getResponseCode()).isEqualTo(200);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String response = reader.readLine();
            assertThat(response).contains("Test successful");
        }
    }

    @Controller(path = "/api")
    static class TestController {
        @Get(path = "/test")
        public ResponseEntity<String> test() {
            return ResponseEntity.ok("Test successful");
        }
    }
}
