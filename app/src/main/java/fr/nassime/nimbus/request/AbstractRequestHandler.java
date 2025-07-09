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

/**
 * AbstractRequestHandler is an abstract implementation of the HttpHandler interface,
 * designed to process HTTP requests. It provides a framework for handling HTTP methods
 * (GET, POST, PUT, DELETE) and sending responses in both plain text and JSON formats.
 * Developers can extend this class to customize the handling of specific HTTP methods.
 */
public class AbstractRequestHandler implements HttpHandler {
    /**
     * Handles an HTTP exchange by delegating processing based on the HTTP request method.
     * Supports GET, POST, PUT, DELETE methods by calling their respective handlers and
     * sends appropriate responses for unsupported methods or errors.
     *
     * @param exchange the HTTP exchange object containing the request and response details
     * @throws IOException if an I/O error occurs while handling the request
     */
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

    /**
     * Handles HTTP GET requests by sending a "405 Method Not Implemented" response.
     *
     * This method is called when a GET request is received by the server, but the functionality
     * for handling GET requests has not been implemented in this handler.
     *
     * @param exchange the HttpExchange object that contains the request details and
     *                 allows writing the response
     * @throws IOException if an I/O error occurs while sending the response
     */
    protected void handleGet(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "GET method not implemented");
    }

    /**
     * Handles HTTP POST requests. This implementation responds with a "405 Method Not Allowed" status
     * and a message stating that the POST method is not implemented.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     * @throws IOException if an I/O error occurs during the response handling
     */
    protected void handlePost(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "POST method not implemented");
    }

    /**
     * Handles HTTP PUT requests received by the server.
     * This implementation sends a 405 (Method Not Allowed) response to indicate
     * that the PUT method is not implemented.
     *
     * @param exchange the HttpExchange object containing request and response details
     * @throws IOException if an I/O error occurs when sending the response
     */
    protected void handlePut(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "PUT method not implemented");
    }

    /**
     * Handles HTTP DELETE requests for the current endpoint. As of the current
     * implementation, this method responds with a 405 (Method Not Allowed) status
     * and indicates that the DELETE method is not implemented.
     *
     * @param exchange the HttpExchange object, which encapsulates the HTTP request and response.
     * @throws IOException if an I/O error occurs during request or response processing.
     */
    protected void handleDelete(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "DELETE method not implemented");
    }

    /**
     * Sends a plain text response to the client with the specified HTTP status code and message.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     * @param statusCode the HTTP status code to be sent in the response
     * @param response the response message to be sent to the client
     * @throws IOException if an I/O error occurs during the response writing process
     */
    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Sends a JSON response to the client using the provided HTTP exchange object.
     * The response is sent with the specified status code and JSON content.
     *
     * @param exchange the HttpExchange object representing the HTTP request-response context.
     * @param statusCode the HTTP status code to include in the response.
     * @param jsonResponse the JSON content to send as the response body.
     * @throws IOException if an I/O error occurs while sending the response.
     */
    protected void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Sends a JSON response to the client using the provided HTTP exchange, status code,
     * and response object. The method converts the provided object to a JSON string and
     * sends it to the client with the specified HTTP status code. If an error occurs during
     * JSON conversion, a 500 status code with an error message is returned.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response context
     * @param statusCode the HTTP status code to be returned in the response
     * @param object the object to be serialized into JSON and sent as the response body
     * @throws IOException if an input or output exception occurs while sending the response
     */
    protected void sendJsonResponse(HttpExchange exchange, int statusCode, Object object) throws IOException {
        try {
            String jsonResponse = JsonUtils.toJson(object);
            sendJsonResponse(exchange, statusCode, jsonResponse);
        } catch (JsonProcessingException e) {
            sendResponse(exchange, 500, "Error converting object to JSON: " + e.getMessage());
        }
    }

    /**
     * Sends an HTTP response based on the provided {@code ResponseEntity} object.
     * If the {@code ResponseEntity} contains a non-null body, a JSON response will
     * be sent with the specified status code. If the body is null, an empty response
     * will be sent with the given status code.
     *
     * @param exchange the current HTTP exchange, which provides the request and response context
     * @param responseEntity the {@code ResponseEntity} containing the status code and body
     *                        for generating the HTTP response
     * @throws IOException if an I/O error occurs while writing the response
     */
    protected void sendResponseEntity(HttpExchange exchange, ResponseEntity<?> responseEntity) throws IOException {
        if (responseEntity.getBody() != null) {
            sendJsonResponse(exchange, responseEntity.getStatus(), responseEntity.getBody());
        } else {
            sendResponse(exchange, responseEntity.getStatus(), "");
        }
    }

    /**
     * Reads and deserializes the request body from an HTTP exchange into an object of the specified class type.
     *
     * @param <T> the type of the object to be deserialized
     * @param exchange the {@code HttpExchange} containing the request whose body is to be read
     * @param clazz the {@code Class} of the object to deserialize the request body into
     * @return an object of type {@code T} representing the deserialized JSON payload
     * @throws IOException if an I/O error occurs while reading the request body,
     *                     or if the request body contains invalid JSON
     */
    protected <T> T readRequestBodyAsObject(HttpExchange exchange, Class<T> clazz) throws IOException {
        String requestBody = readRequestBody(exchange);
        try {
            return JsonUtils.fromJson(requestBody, clazz);
        } catch (JsonProcessingException e) {
            throw new IOException("Invalid JSON format: " + e.getMessage(), e);
        }
    }

    /**
     * Reads the request body from the provided HTTP exchange and returns it as a UTF-8 encoded string.
     *
     * @param exchange the HTTP exchange containing the request body to be read
     * @return the request body as a string
     * @throws IOException if an I/O error occurs while reading the request body
     */
    protected String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Parses a query string into a map of key-value pairs.
     * The query string is expected to be in the format "key1=value1&key2=value2".
     * If the query string is null or empty, an empty map is returned.
     * Invalid pairs (i.e., those without exactly one '=' character) are ignored.
     *
     * @param query the query string to parse
     * @return a map containing the parsed key-value pairs from the query string
     */
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

    /**
     * Retrieves the path parameters from the provided HttpExchange object.
     *
     * This method fetches the "pathParams" attribute from the HttpExchange instance,
     * which is expected to be a map of parameter names to their respective values.
     * If the "pathParams" attribute is not present or is null, it returns an empty map.
     *
     * @param exchange the HttpExchange instance containing the path parameters as an attribute
     * @return a map of path parameter names to their corresponding values; returns an empty map if no path parameters exist
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getPathParams(HttpExchange exchange) {
        Map<String, String> pathParams = (Map<String, String>) exchange.getAttribute("pathParams");
        return pathParams != null ? pathParams : new HashMap<>();
    }

    /**
     * Retrieves the value of a specific path parameter from the given HTTP exchange.
     *
     * @param exchange the {@code HttpExchange} object from which path parameters are derived
     * @param paramName the name of the path parameter to retrieve
     * @return the value of the specified path parameter, or {@code null} if the parameter is not found
     */
    public String getPathParam(HttpExchange exchange, String paramName) {
        Map<String, String> pathParams = getPathParams(exchange);
        return pathParams.get(paramName);
    }
}
