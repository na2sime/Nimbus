package fr.nassime.nimbus.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.nassime.nimbus.http.ResponseEntity;
import fr.nassime.nimbus.utils.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AbstractRequestHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "PUT":
                    handlePut(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Unauthorized method: " + method);
                    break;
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    protected void handleGet(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "GET method not implemented");
    }

    protected void handlePost(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "POST method not implemented");
    }

    protected void handlePut(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "PUT method not implemented");
    }

    protected void handleDelete(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "DELETE method not implemented");
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected void sendJsonResponse(HttpExchange exchange, int statusCode, Object object) throws IOException {
        try {
            String jsonResponse = JsonUtils.toJson(object);
            sendJsonResponse(exchange, statusCode, jsonResponse);
        } catch (JsonProcessingException e) {
            sendResponse(exchange, 500, "Error converting object to JSON: " + e.getMessage());
        }
    }

    protected void sendResponseEntity(HttpExchange exchange, ResponseEntity<?> responseEntity) throws IOException {
        if (responseEntity.getBody() != null) {
            sendJsonResponse(exchange, responseEntity.getStatus(), responseEntity.getBody());
        } else {
            sendResponse(exchange, responseEntity.getStatus(), "");
        }
    }

    protected <T> T readRequestBodyAsObject(HttpExchange exchange, Class<T> clazz) throws IOException {
        String requestBody = readRequestBody(exchange);
        try {
            return JsonUtils.fromJson(requestBody, clazz);
        } catch (JsonProcessingException e) {
            throw new IOException("Invalid JSON format: " + e.getMessage(), e);
        }
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }

        return params;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getPathParams(HttpExchange exchange) {
        Map<String, String> pathParams = (Map<String, String>) exchange.getAttribute("pathParams");
        return pathParams != null ? pathParams : new HashMap<>();
    }

    public String getPathParam(HttpExchange exchange, String paramName) {
        Map<String, String> pathParams = getPathParams(exchange);
        return pathParams.get(paramName);
    }
}
