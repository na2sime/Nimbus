
package fr.nassime.nimbus.api.request;

import com.sun.net.httpserver.HttpExchange;
import fr.nassime.nimbus.api.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

public interface IRequestHandler {
    void handle(HttpExchange exchange) throws IOException;

    void handleGet(HttpExchange exchange) throws IOException;

    void handlePost(HttpExchange exchange) throws IOException;

    void handlePut(HttpExchange exchange) throws IOException;

    void handleDelete(HttpExchange exchange) throws IOException;

    void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException;

    void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException;

    void sendJsonResponse(HttpExchange exchange, int statusCode, Object object) throws IOException;

    void sendResponseEntity(HttpExchange exchange, ResponseEntity<?> responseEntity) throws IOException;

    <T> T readRequestBodyAsObject(HttpExchange exchange, Class<T> clazz) throws IOException;

    String readRequestBody(HttpExchange exchange) throws IOException;

    Map<String, String> parseQueryParams(String query);

    Map<String, String> getPathParams(HttpExchange exchange);

    String getPathParam(HttpExchange exchange, String paramName);
}
