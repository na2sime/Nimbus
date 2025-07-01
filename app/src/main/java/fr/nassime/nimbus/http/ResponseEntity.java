package fr.nassime.nimbus.http;

import lombok.Getter;
import lombok.Setter;

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

    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(body, 200);
    }

    public static <T> ResponseEntity<T> created(T body) {
        return new ResponseEntity<>(body, 201);
    }

    public static <T> ResponseEntity<T> notFound() {
        return new ResponseEntity<>(null, 404);
    }

    public static <T> ResponseEntity<T> badRequest(T body) {
        return new ResponseEntity<>(body, 400);
    }
}
