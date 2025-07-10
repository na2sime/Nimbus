
package fr.nassime.nimbus.routing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.nassime.nimbus.annotations.middleware.WithMiddleware;
import fr.nassime.nimbus.annotations.request.PathVariable;
import fr.nassime.nimbus.annotations.request.RequestBody;
import fr.nassime.nimbus.annotations.type.Delete;
import fr.nassime.nimbus.annotations.type.Get;
import fr.nassime.nimbus.annotations.type.Post;
import fr.nassime.nimbus.annotations.type.Put;
import fr.nassime.nimbus.middleware.Middleware;
import fr.nassime.nimbus.request.AbstractRequestHandler;
import fr.nassime.nimbus.http.ResponseEntity;
import fr.nassime.nimbus.utils.JsonUtils;
import fr.nassime.nimbus.controller.Controller;
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

/**
 * The Router class represents an abstract HTTP request handler that manages routing logic.
 * It provides mechanisms for registering routes and controllers, handling HTTP requests,
 * and invoking appropriate methods on controllers based on annotations. It also supports
 * middleware functionality for processing requests.
 */
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

    /**
     * Abstract method to initialize route mappings for the Router.
     *
     * This method is called during the construction of the Router class and must be implemented
     * by subclasses to define the particular routes handled by the application. Implementations
     * should register paths and their corresponding handlers using methods like {@code register}.
     *
     * Routes define the association between HTTP request paths and the handlers that process
     * those requests. Subclasses can structure routes, use base paths for controllers, and
     * set up middleware where applicable to customize route handling.
     */
    protected abstract void initRoutes();

    /**
     * Registers a new route with a given path and HTTP handler.
     *
     * @param path the HTTP request path to be associated with the handler
     * @param handler the HTTP handler to process requests for the specified path
     */
    public void register(String path, HttpHandler handler) {
        routes.add(new Route(path, handler));
    }

    /**
     * Registers a controller for routing by analyzing its class for annotations or implementing interfaces.
     * If the provided object is annotated with {@link fr.nassime.nimbus.annotations.Controller}, its methods
     * will be inspected and registered as routes based on the annotation's attributes.
     *
     * If the controller object implements the {@link Controller} interface, it delegates its registration
     * process to the controller's {@code register} method.
     *
     * @param controller the controller object to be registered. This object should either be:
     *                   - Annotated with {@link fr.nassime.nimbus.annotations.Controller}, or
     *                   - An implementation of the {@link Controller} interface.
     */
    public void registerController(Object controller) {
        Class<?> controllerClass = controller.getClass();
        fr.nassime.nimbus.annotations.Controller annotation = controllerClass.getAnnotation(fr.nassime.nimbus.annotations.Controller.class);

        if (annotation != null) {
            String basePath = annotation.path();

            for (Method method : controllerClass.getDeclaredMethods()) {
                registerMethodAsRoute(controller, method, basePath);
            }
        } else if (controller instanceof Controller) {
            ((Controller) controller).register(this);
        }
    }

    /**
     * Registers a method of a controller as a route based on HTTP annotations such as {@code @Get}, {@code @Post},
     * {@code @Put}, and {@code @Delete}. The method's path is derived from the specified base path concatenated
     * with the relative path defined in the annotation. The method is associated with the correct HTTP handler
     * to process requests.
     *
     * @param controller The controller object containing the method being registered.
     * @param method The method of the controller that will be registered as a route.
     * @param basePath The base path prefix for the route.
     */
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

    /**
     * Creates a handler for the specified controller method, enabling it to handle HTTP requests
     * in the defined routing structure. This method wraps the provided method with middleware
     * support and handles the invocation of the controller method while managing responses and errors.
     *
     * @param controller the controller instance containing the method to be executed
     * @param method the controller method to be wrapped and executed for handling HTTP requests
     * @return a {@code HandleMethod} instance that encapsulates the logic for invoking the method,
     *         handling responses, and managing middleware
     */
    private HandleMethod createHandlerForMethod(Object controller, Method method) {
        HandleMethod originalHandler = exchange -> {
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

        return wrapWithMiddleware(originalHandler, method, controller);
    }

    /**
     * Extracts method parameters by analyzing the provided {@link Method} and the {@link HttpExchange}.
     * This method resolves the values for annotated parameters (e.g., {@link PathVariable} and {@link RequestBody})
     * and binds them accordingly. If a parameter is of type {@link HttpExchange}, the current exchange object is passed directly.
     *
     * The extraction involves:
     * - Resolving path variables annotated with {@link PathVariable}, using the provided {@link HttpExchange}.
     * - Parsing the request body into an object when annotated with {@link RequestBody}.
     * - Passing the {@link HttpExchange} object when requested directly as a parameter.
     *
     * @param method   the {@link Method} being invoked, containing parameter metadata and annotations
     * @param exchange the {@link HttpExchange} representing the current HTTP request and response context
     * @return an array of objects representing the resolved and type-cast parameters for the method invocation
     * @throws IOException if an error occurs while reading the request body or resolving parameters
     */
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

    /**
     * Executes the middlewares associated with the specified controller class and method,
     * applying them to the given HTTP exchange. The middlewares are processed in the order
     * they are defined, and each middleware's `handle` method is invoked.
     *
     * If any middleware returns {@code false}, the middleware chain is terminated, and
     * further processing does not occur. Otherwise, all middlewares are invoked sequentially.
     *
     * @param method the {@link Method} which represents the method in the controller
     *               being executed. This is used to retrieve method-level middlewares.
     * @param controller the object instance of the controller containing the method.
     *                   This is used to retrieve class-level middlewares.
     * @param exchange the {@link HttpExchange} object representing the HTTP request
     *                 and response, which is passed to the middleware `handle` method.
     * @return a list of {@link Middleware} objects that were executed, or {@code null}
     *         if any middleware stopped the execution by returning {@code false}.
     * @throws IOException if an input/output error occurs during middleware processing.
     */
    private List<Middleware> executeMiddlewares(Method method, Object controller, HttpExchange exchange) throws IOException {
        List<Middleware> middlewares = new ArrayList<>();

        WithMiddleware[] classMiddlewares = controller.getClass().getAnnotationsByType(WithMiddleware.class);
        for (WithMiddleware middleware : classMiddlewares) {
            try {
                middlewares.add(middleware.value().getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                log.error("Error instantiating middleware: {}", middleware.value().getName(), e);
            }
        }

        WithMiddleware[] methodMiddlewares = method.getAnnotationsByType(WithMiddleware.class);
        for (WithMiddleware middleware : methodMiddlewares) {
            try {
                middlewares.add(middleware.value().getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                log.error("Error instantiating middleware: {}", middleware.value().getName(), e);
            }
        }

        for (Middleware middleware : middlewares) {
            if (!middleware.handle(exchange)) {
                return null;
            }
        }

        return middlewares;
    }

    /**
     * Wraps the provided handler method with middleware processing logic.
     * This method ensures that the specified middleware, defined at the method or controller level,
     * is executed before the provided handler is invoked. If any middleware in the chain halts further
     * processing, the handler is not executed.
     *
     * @param originalHandler the original handle method to be wrapped; this is the final handler to be executed
     *                        if all middleware in the chain approve continuation.
     * @param method the method associated with the route, potentially annotated with middleware definitions,
     *               used to determine the middleware to apply.
     * @param controller the controller instance containing the method; annotations on the controller class can
     *                   also define middleware to apply.
     * @return a wrapped {@code HandleMethod} that incorporates middleware processing logic along with the
     *         provided original handler.
     */
    private HandleMethod wrapWithMiddleware(HandleMethod originalHandler, Method method, Object controller) {
        return exchange -> {
            List<Middleware> middlewares = executeMiddlewares(method, controller, exchange);
            if (middlewares != null) {
                originalHandler.handle(exchange);
            }
        };
    }


    /**
     * Converts a string value into an instance of the specified target type.
     * Supports conversion for common types such as String, Integer, Long, Double, and Boolean.
     * For unsupported target types, the original string value is returned.
     *
     * @param value the string value to be converted
     * @param targetType the class object representing the target type
     * @return an object of the specified target type, or the original string value if the type is unsupported
     */
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

    /**
     * Handles incoming HTTP requests by matching the request URI path against registered routes.
     * If a matching route is found, it delegates the request handling to the corresponding handler
     * after extracting path parameters and setting them as attributes in the exchange object.
     * If no matching route is found, the request is delegated to a predefined "not found" handler.
     *
     * @param exchange the {@link HttpExchange} object representing the HTTP request and response context.
     * @throws IOException if an I/O error occurs during request handling.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

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

    /**
     * Returns an implementation of {@link AbstractRequestHandler} that processes HTTP GET requests
     * using the specified {@link HandleMethod}.
     *
     * @param method the {@link HandleMethod} instance responsible for handling the HTTP GET request.
     *               This method encapsulates the logic for processing the request.
     * @return an instance of {@link AbstractRequestHandler} configured to handle HTTP GET requests
     *         and delegate the request handling logic to the provided {@link HandleMethod}.
     */
    protected AbstractRequestHandler get(HandleMethod method) {
        return new AbstractRequestHandler() {
            @Override
            protected void handleGet(HttpExchange exchange) throws IOException {
                method.handle(exchange);
            }
        };
    }

    /**
     * Creates and returns an {@link AbstractRequestHandler} for handling HTTP POST requests.
     * The provided {@code method} is executed when a corresponding request is received.
     *
     * @param method the handler implementation defining logic to process the POST request.
     * @return an {@link AbstractRequestHandler} configured to handle POST requests.
     */
    protected AbstractRequestHandler post(HandleMethod method) {
        return new AbstractRequestHandler() {
            @Override
            protected void handlePost(HttpExchange exchange) throws IOException {
                method.handle(exchange);
            }
        };
    }

    /**
     * Creates and returns an AbstractRequestHandler for handling HTTP PUT requests.
     * The provided HandleMethod instance is used to process the PUT request.
     *
     * @param method the HandleMethod instance that defines the logic for handling the HTTP PUT request
     * @return an AbstractRequestHandler that delegates the handling of the PUT request to the provided HandleMethod
     */
    protected AbstractRequestHandler put(HandleMethod method) {
        return new AbstractRequestHandler() {
            @Override
            protected void handlePut(HttpExchange exchange) throws IOException {
                method.handle(exchange);
            }
        };
    }

    /**
     * Creates an HTTP DELETE request handler using the provided method implementation.
     *
     * @param method the {@link HandleMethod} instance that defines the behavior of the DELETE request handler
     * @return an {@link AbstractRequestHandler} instance configured to handle DELETE requests
     */
    protected AbstractRequestHandler delete(HandleMethod method) {
        return new AbstractRequestHandler() {
            @Override
            protected void handleDelete(HttpExchange exchange) throws IOException {
                method.handle(exchange);
            }
        };
    }

    @FunctionalInterface
    public interface HandleMethod {
        void handle(HttpExchange exchange) throws IOException;
    }

    /**
     * Extracts the value of the specified path parameter from the given HTTP exchange.
     * The path parameters are expected to be stored as a Map attribute in the exchange under
     * the key "pathParams".
     *
     * @param exchange the HttpExchange object containing the request information and attributes
     * @param paramName the name of the path parameter to retrieve
     * @return the value of the specified path parameter, or null if the parameter does not exist
     */
    protected String getPathParam(HttpExchange exchange, String paramName) {
        @SuppressWarnings("unchecked")
        Map<String, String> pathParams = (Map<String, String>) exchange.getAttribute("pathParams");
        return pathParams != null ? pathParams.get(paramName) : null;
    }

    /**
     * Sends an HTTP response to the client with a specified status code and response body.
     *
     * This method prepares the response by setting the appropriate headers, encoding the response
     * body in UTF-8, and writing it to the response output stream. It ensures proper handling of
     * the response body by automatically closing the output stream after writing.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     * @param statusCode the HTTP status code to be sent in the response
     * @param response the response body content to be sent to the client
     * @throws IOException if an I/O error occurs while sending the response
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
     * Sends a JSON response to the client using the provided HTTP exchange object, status code,
     * and JSON response body. The method sets the required HTTP response headers to indicate
     * the content type as JSON and writes the response body to the output stream of the exchange.
     *
     * @param exchange the HttpExchange object that represents the HTTP request and response
     * @param statusCode the HTTP status code to be sent in the response
     * @param jsonResponse the JSON string to be sent as the response body
     * @throws IOException if an I/O error occurs while sending the response
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
     * Sends a JSON response to the client using the provided {@code HttpExchange}.
     * Converts the given object to a JSON string and sends it with the specified HTTP status code.
     * If an error occurs while converting the object to JSON, a 500 status code with an error message
     * will be sent instead.
     *
     * @param exchange the {@code HttpExchange} object representing the ongoing HTTP transaction
     * @param statusCode the HTTP status code to set for the response
     * @param object the object to be serialized to JSON and sent in the response body
     * @throws IOException if an I/O error occurs while sending the response
     */
    protected void sendJsonResponse(HttpExchange exchange, int statusCode, Object object) throws IOException {
        try {
            String jsonResponse = JsonUtils.toJson(object);
            sendJsonResponse(exchange, statusCode, jsonResponse);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON: {}", object, e);
            sendJsonResponse(exchange, 500, "Error converting object to JSON: " + e.getMessage());
        }
    }

    /**
     * Reads the request body from an {@link HttpExchange} and deserializes it into an object of the specified type.
     *
     * This method attempts to read the request body as a UTF-8 encoded string and parse it as a JSON object.
     * If the parsing fails, an {@link IOException} is thrown.
     *
     * @param <T>   the type of the object to deserialize the request body into
     * @param exchange the {@link HttpExchange} containing the request data
     * @param clazz    the {@link Class} object of the type to deserialize the JSON request body into
     * @return the deserialized object of type {@code T}
     * @throws IOException if an I/O error occurs while reading the request body or if JSON parsing fails
     */
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
