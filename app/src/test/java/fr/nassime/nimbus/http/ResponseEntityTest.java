package fr.nassime.nimbus.http;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ResponseEntityTest {

    @Test
    void shouldCreateOkResponse() {
        // Given & When
        ResponseEntity<String> response = ResponseEntity.ok("Success");

        // Then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Success");
        assertThat(response.getContentType()).isEqualTo("application/json");
    }

    @Test
    void shouldCreateCreatedResponse() {
        // Given & When
        ResponseEntity<String> response = ResponseEntity.created("Created");

        // Then
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo("Created");
    }

    @Test
    void shouldCreateNotFoundResponse() {
        // Given & When
        ResponseEntity<String> response = ResponseEntity.notFound();

        // Then
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldCreateBadRequestResponse() {
        // Given & When
        ResponseEntity<String> response = ResponseEntity.badRequest("Bad Request");

        // Then
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Bad Request");
    }

    @Test
    void shouldCreateCustomResponse() {
        // Given & When
        ResponseEntity<String> response = new ResponseEntity<>("Custom", 418);

        // Then
        assertThat(response.getStatus()).isEqualTo(418); // I'm a teapot
        assertThat(response.getBody()).isEqualTo("Custom");
    }
}
