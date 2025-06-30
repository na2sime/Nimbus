
package fr.nassime.nimbus.api.routing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.nassime.nimbus.api.annotations.*;
import fr.nassime.nimbus.api.request.AbstractRequestHandler;
import fr.nassime.nimbus.api.http.ResponseEntity;
import fr.nassime.nimbus.api.request.IRequestHandler;
import fr.nassime.nimbus.api.utils.JsonUtils;
import fr.nassime.nimbus.api.controller.Controller;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class Router implements HttpHandler {

    private final List<Route> routes = new ArrayList<>();
    @Setter
    private HttpHandler notFoundHandler;

    public Router() {
        notFoundHandler = exchange -> {
            String response = "404 - Route not found: " + exchange.getRequestURI().getPath();
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(404, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        };

        initRoutes();
    }

    protected abstract void initRoutes();

    public void register(String path, HttpHandler handler) {
        routes.add(new Route(path, handler));
    }

    public void registerController(Object controller) {
        Class<?> controllerClass = controller.getClass();
        fr.nassime.nimbus.api.annotations.Controller annotation = controllerClass.getAnnotation(fr.nassime.nimbus.api.annotations.Controller.class);

        if (annotation != null) {
            String basePath = annotation.path();

            for (Method method : controllerClass.getDeclaredMethods()) {
                registerMethodAsRoute(controller, method, basePath);
            }
        } else if (controller instanceof Controller) {
            ((Controller) controller).register(this);
        }
    }

    private void registerMethodAsRoute(Object controller, Method method, String basePath) {
        StringBuilder pathBuilder = new StringBuilder(basePath);

        if (method.isAnnotationPresent(Get.class)) {
            pathBuilder.append(method.getAnnotation(Get.class).path());
            register(pathBuilder.toString(), get(createHandlerForMethod(controller, method)));
        } else if (method.isAnnotationPresent(Post.class)) {
            pathBuilder.append(method.getAnnotation(Post.class).path());
            register(pathBuilder.toString(), post(createHandlerForMethod(controller, method)));
        } else if (method.isAnnotationPresent(Put.class)) {
            pathBuilder.append(method.getAnnotation(Put.class).path());
            register(pathBuilder.toString(), put(createHandlerForMethod(controller, method)));
        } else if (method.isAnnotationPresent(Delete.class)) {
            pathBuilder.append(method.getAnnotation(Delete.class).path());
            register(pathBuilder.toString(), delete(createHandlerForMethod(controller, method)));
        }
    }

    private HandleMethod createHandlerForMethod(Object controller, Method method) {
        return exchange -> {
            try {
                Object[] args = extractMethodParameters(method, exchange);
                method.setAccessible(true);
                Object result = method.invoke(controller, args);

                if (result instanceof ResponseEntity) {
                    ResponseEntity<?> response = (ResponseEntity<?>) result;
                    if (response.getBody() != null) {
                        sendJsonResponse(exchange, response.getStatus(), response.getBody());
                    } else {
                        sendResponse(exchange, response.getStatus(), "");
                    }
                } else if (result != null) {
                    sendJsonResponse(exchange, 200, result);
                } else {
                    sendResponse(exchange, 204, "");
                }
            } catch (Exception e) {
                log.error("Error invoking controller method: {}", method.getName(), e);
                log.error("Request URI: {}", exchange.getRequestURI());
                log.error("Request method: {}", exchange.getRequestMethod());
                log.error("Request headers: {}", exchange.getRequestHeaders());
                sendResponse(exchange, 500, "Server error: " + e.getMessage());
            }
        };
    }

    private Object[] extractMethodParameters(Method method, HttpExchange exchange) throws IOException {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            if (param.isAnnotationPresent(PathVariable.class)) {
                PathVariable pathVar = param.getAnnotation(PathVariable.class);
                String paramName = pathVar.value().isEmpty() ? param.getName() : pathVar.value();
                String value = getPathParam(exchange, paramName);
                args[i] = convertValue(value, param.getType());
            } else if (param.isAnnotationPresent(RequestBody.class)) {
                args[i] = readRequestBodyAsObject(exchange, param.getType());
            } else if (param.getType() == HttpExchange.class) {
                args[i] = exchange;
            }
        }

        return args;
    }

    private Object convertValue(String value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType == String.class) {
            return value;
        }
        if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        }
        if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        }
        if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // Vérifier si nous sommes dans un sous-chemin
        Object uriPath = exchange.getAttribute("uri-path");
        if (uriPath != null) {
            path = (String) uriPath;
        }

        for (Route route : routes) {
            if (route.matches(path)) {
                String[] paramValues = route.extractPathParams(path);
                String[] paramNames = route.getParamNames();

                Map<String, String> pathParams = new HashMap<>();
                for (int i = 0; i < Math.min(paramNames.length, paramValues.length); i++) {
                    pathParams.put(paramNames[i], paramValues[i]);
                }

                exchange.setAttribute("pathParams", pathParams);
                exchange.setAttribute("handler", route.getHandler());

                route.getHandler().handle(exchange);
                return;
            }
        }

        notFoundHandler.handle(exchange);
    }

    protected IRequestHandler get(HandleMethod method) {
        return new AbstractRequestHandler() {
            @Override
            public void handleGet(HttpExchange exchange) throws IOException {
                method.handle(exchange);
            }
        };
    }

    protected IRequestHandler post(HandleMethod method) {
        return new AbstractRequestHandler() {
            @Override
            public void handlePost(HttpExchange exchange) throws IOException {
                method.handle(exchange);
            }
        };
    }

    protected IRequestHandler put(HandleMethod method) {
        return new AbstractRequestHandler() {
            @Override
            public void handlePut(HttpExchange exchange) throws IOException {
                method.handle(exchange);
            }
        };
    }

    protected IRequestHandler delete(HandleMethod method) {
        return new AbstractRequestHandler() {
            @Override
            public void handleDelete(HttpExchange exchange) throws IOException {
                method.handle(exchange);
            }
        };
    }

    @FunctionalInterface
    public interface HandleMethod {
        void handle(HttpExchange exchange) throws IOException;
    }

    protected String getPathParam(HttpExchange exchange, String paramName) {
        @SuppressWarnings("unchecked")
        Map<String, String> pathParams = (Map<String, String>) exchange.getAttribute("pathParams");
        return pathParams != null ? pathParams.get(paramName) : null;
    }

    // Méthodes d'aide pour les réponses HTTP
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
            log.error("Error converting object to JSON: {}", object, e);
            sendJsonResponse(exchange, 500, "Error converting object to JSON: " + e.getMessage());
        }
    }

    protected <T> T readRequestBodyAsObject(HttpExchange exchange, Class<T> clazz) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            return JsonUtils.fromJson(requestBody, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON request body: {}", requestBody, e);
            throw new IOException("JSON format invalid: " + e.getMessage(), e);
        }
    }
}
