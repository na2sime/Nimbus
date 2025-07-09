package fr.nassime.nimbus.http;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a response entity that encapsulates the HTTP response status, body,
 * and content type. Designed for use in server-side applications to standardize
 * and simplify the construction of HTTP responses.
 *
 * <h3>Overview:</h3>
 * The Generic parameter {@code T} allows the ResponseEntity to handle responses
 * with different types of body content like String, JSON objects, or other custom types.
 *
 * <h3>Key Features:</h3>
 * - Provides factory methods for commonly used HTTP responses such as OK (200), Created (201),
 *   Not Found (404), and Bad Request (400).
 * - Ensures default `Content-Type` of `application/json`.
 *
 * <h3>Constructors:</h3>
 * - {@code ResponseEntity(T body, int status)}: Constructor that initializes the
 *   response entity with a body and HTTP status code. The default content type
 *   for responses is `application/json`.
 *
 * <h3>Factory Methods:</h3>
 * - {@code ok(T body)}: Creates a new ResponseEntity with an HTTP status code of 200
 *   (OK) and the given body.
 * - {@code created(T body)}: Creates a new ResponseEntity with an HTTP status code of
 *   201 (Created) and the given body.
 * - {@code notFound()}: Creates a new ResponseEntity with an HTTP status code of 404
 *   (Not Found) and no body.
 * - {@code badRequest(T body)}: Creates a new ResponseEntity with an HTTP status
 *   code of 400 (Bad Request) and the given body.
 *
 * @param <T> The type of the response body.
 */
@Getter
@Setter
public class ResponseEntity<T> {
    private int status;
    private T body;
    private String contentType;

    public ResponseEntity(T body, int status) {
        this.body = body;
        this.status = status;
        this.contentType = "application/json";
    }

    /**
     * Creates a new {@code ResponseEntity} instance with an HTTP status code of 200 (OK)
     * and the specified response body.
     *
     * @param <T> the type of the response body
     * @param body the body of the response
     * @return a new {@code ResponseEntity} with the given body and a status code of 200
     */
    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(body, 200);
    }

    /**
     * Creates a new {@code ResponseEntity} with an HTTP status code of 201 (Created)
     * and the given body as the response content.
     *
     * @param <T> the type of the response body
     * @param body the content to include in the response body
     * @return a {@code ResponseEntity} instance with the specified body and a status code of 201
     */
    public static <T> ResponseEntity<T> created(T body) {
        return new ResponseEntity<>(body, 201);
    }

    /**
     * Creates a ResponseEntity with an HTTP status code of 404 (Not Found) and no body content.
     *
     * This method provides a standardized way to represent a "Not Found" response in server-side
     * applications when the requested resource cannot be located. The response body is set to null.
     *
     * @return a ResponseEntity with a status code of 404 and no content in the body
     */
    public static <T> ResponseEntity<T> notFound() {
        return new ResponseEntity<>(null, 404);
    }

    /**
     * Creates a ResponseEntity with an HTTP status code of 400 (Bad Request)
     * and the specified body.
     *
     * @param <T> The type of the response body.
     * @param body The body of the response to be included in the ResponseEntity.
     * @return A ResponseEntity representing a Bad Request (400) response with the specified body.
     */
    public static <T> ResponseEntity<T> badRequest(T body) {
        return new ResponseEntity<>(body, 400);
    }
}
