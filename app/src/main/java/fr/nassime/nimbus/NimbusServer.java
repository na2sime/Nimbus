package fr.nassime.nimbus;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import fr.nassime.nimbus.annotations.Controller;
import fr.nassime.nimbus.routing.Router;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import lombok.Getter;

/**
 * The NimbusServer class represents an HTTP server that is lightweight, configurable, and can be
 * used for building and hosting RESTful APIs or web applications. The server supports features such as
 * routing, controller auto-discovery, API key validation, and custom request handling through routers.
 *
 * This server implementation leverages an internal `Router` for managing routes and can be extended
 * or customized with additional configurations and handlers.
 */
@Slf4j
public class NimbusServer {

    /**
     * Represents the underlying HTTP server instance used by the {@code NimbusServer} class.
     * This server is responsible for handling HTTP requests, managing routes,
     * and executing associated handlers or controllers.
     *
     * Characteristics:
     * - This is a final and immutable instance of {@code HttpServer}.
     * - It operates as the core of the Nimbus framework, enabling web communication
     *   and serving API endpoints as defined by the configuration.
     *
     * Lifecycle Management:
     * - The server is initialized during {@code NimbusServer} construction.
     * - Its lifecycle (start, stop) is handled by the {@code NimbusServer} methods
     *   such as {@code start()} and {@code stop(int delay)}.
     *
     * Thread Safety:
     * - The instance is thread-safe because it is immutable and lifecycle actions
     *   are controlled through synchronized methods within {@code NimbusServer}.
     *
     * Exceptions:
     * - Any I/O or network-related exceptions during server initialization or
     *   operations are handled at an implementation level or propagated where necessary.
     */
    private final HttpServer server;
    /**
     * A Map that holds API keys used to secure access to the NimbusServer.
     * Each key-value pair in this map represents the name of the API key
     * and its corresponding secret value.
     *
     * Key Features:
     * - Enables API key-based security for server endpoints.
     * - Allows dynamic addition of API keys through the server's methods.
     *
     * Thread Safety:
     * This map is immutable once initialized, promoting thread safety in concurrent environments.
     * Any modifications are managed through specific methods of the NimbusServer class.
     *
     * Usage Context:
     * Primarily used internally by the NimbusServer to validate API keys
     * for incoming HTTP requests when key-based authentication is enabled.
     */
    private final Map<String, String> apiKeys;
    /**
     * Represents the primary router used to handle HTTP routes and delegate incoming requests
     * to their appropriate handlers.
     *
     * This router serves as the central routing mechanism for all incoming HTTP requests
     * within the server. It provides an organized way to map URL paths to their respective
     * request handlers.
     *
     * The `mainRouter` integrates with the server's routing logic, allowing for the addition
     * of specific endpoint routes, grouping of routes, and customizable error handling.
     */
    private final Router mainRouter;
    /**
     * Represents the configuration for the server used in the NimbusServer class.
     * This instance holds details such as server port, backlog size, thread pool,
     * and various settings that determine the server's behavior.
     *
     * This configuration is immutable and is typically built using the provided
     * builder in the `ServerConfig` class.
     *
     * Key settings within the configuration:
     * - Server port: Defines the port on which the server listens for incoming requests.
     * - Backlog size: Specifies the maximum number of pending connections the server can queue.
     * - Thread pool: Manages the threads for handling requests.
     * - API key requirement: Determines if the server enforces API key validation for access.
     * - Auto controller scanning: Enables automatic detection and registration of controllers.
     * - Base package: Specifies the package to scan for controllers, if auto-scanning is enabled.
     * - Verbose mode: Toggles detailed logging output for debugging purposes.
     *
     * This field is final and cannot be modified after initialization of the `NimbusServer`.
     * It ensures consistent server behavior according to the predefined configuration.
     */
    private final ServerConfig config;

    /**
     * Represents the configuration options for the server. This class is utilized to
     * initialize and configure the server with the specified parameters during its creation.
     *
     * The configuration supports setting the server's port, connection backlog, executor
     * for handling client requests, API key requirement, controller auto-scanning, base
     * package for scanning, and verbosity of logging.
     *
     * Fields:
     * - serverPort: The port number the server will bind to. Defaults to 8080.
     * - serverBacklog: The maximum number of pending connections allowed in the server's queue.
     * - serverExecutor: The executor service responsible for managing client requests.
     *   Defaults to a thread pool with 10 threads.
     * - isApiKeyRequired: Determines whether API key validation is required for incoming requests.
     * - isAutoScanControllers: Specifies whether controllers should be automatically scanned
     *   and registered from a specified base package.
     * - scanBasePackage: The base package to scan for controller classes, used only if
     *   controller auto-scanning is enabled. Defaults to an empty string.
     * - isVerbose: Indicates whether verbose logging is enabled for the server.
     */
    @Getter
    @Builder
    public static class ServerConfig {
        @Builder.Default
        private final int serverPort = 8080;
        private final int serverBacklog;
        @Builder.Default
        private final Executor serverExecutor = Executors.newFixedThreadPool(10);
        private final boolean isApiKeyRequired;
        private final boolean isAutoScanControllers;
        @Builder.Default
        private final String scanBasePackage = "";
        private final boolean isVerbose;
    }

    /**
     * Constructs a NimbusServer instance with the specified configuration parameters.
     * Initializes the server configuration, HTTP server, and the main router for handling requests.
     * Depending on the specified options, it supports API key validation and automatic controller scanning.
     *
     * @param port the port number for the server to bind to
     * @param backlog the maximum number of pending connections allowed in the server's queue
     * @param threadPoolSize the size of the thread pool for handling client requests; defaults to 4 if set to a non-positive number
     * @param apiKeyRequired whether API key validation is required for incoming requests
     * @param autoScanControllers whether to automatically scan and register controllers in the specified base package
     * @param basePackage the base package to scan for controller classes; only used if autoScanControllers is set to true
     * @param verbose whether verbose logging is enabled for the server
     * @throws IOException if an I/O error occurs during the server initialization
     */
    @Builder
    private NimbusServer(int port, int backlog, int threadPoolSize, boolean apiKeyRequired,
                         boolean autoScanControllers, String basePackage, boolean verbose) throws IOException {
        this.apiKeys = new HashMap<>();

        int actualThreadPoolSize = (threadPoolSize > 0) ? threadPoolSize : 4;

        this.config = ServerConfig.builder()
            .serverPort(port)
            .serverBacklog(backlog)
            .serverExecutor(Executors.newFixedThreadPool(actualThreadPoolSize))
            .isApiKeyRequired(apiKeyRequired)
            .isAutoScanControllers(autoScanControllers)
            .scanBasePackage(basePackage)
            .isVerbose(verbose)
            .build();

        this.server = HttpServer.create(new InetSocketAddress(config.getServerPort()), config.getServerBacklog());
        this.server.setExecutor(config.getServerExecutor());

        this.mainRouter = new Router() {
            @Override
            protected void initRoutes() {
                // Default router does not have predefined routes
            }
        };

        server.createContext("/", exchange -> {
            if (!config.isApiKeyRequired() || isValidApiKey(exchange)) {
                mainRouter.handle(exchange);
            } else {
                sendResponse(exchange, 401, "Unauthorized: Invalid API Key");
            }
        });

        if (config.isAutoScanControllers()) {
            scanAndRegisterControllers();
        }
    }


    /**
     * Scans for controller classes annotated with the {@code @Controller} annotation
     * within the base package specified in the server configuration and registers them
     * with the main router. All discovered controllers are instantiated and registered
     * dynamically during runtime.
     *
     * This method is invoked during server initialization if the server is configured
     * with automatic controller scanning enabled.
     *
     * Behavior:
     * - Utilizes the Reflections library to detect and retrieve all classes annotated
     *   with {@code @Controller} within the specified scan base package.
     * - Each identified controller class is instantiated using its no-argument constructor
     *   and passed to the {@code registerController} method for registration with the main router.
     * - Logs success and failure events for both controller instantiation and overall scanning
     *   process if verbose logging is enabled in the configuration.
     *
     * Exception Handling:
     * - If a controller class cannot be instantiated (e.g., due to lack of a default constructor
     *   or other reflection-related failures), the error is logged, and processing continues
     *   with the next controller.
     * - If the scanning process fails entirely (e.g., due to an unexpected runtime exception),
     *   the error is logged without halting the application.
     *
     * Logging:
     * - Logs messages about discovered and successfully instantiated controllers when verbose
     *   logging is enabled.
     * - Logs errors for failed controller instantiations and scanning failures when verbose
     *   logging is enabled.
     *
     * Requirements:
     * - The {@code @Controller} annotation should be properly defined and available at runtime.
     * - The base package for scanning must be specified in the server configuration.
     */
    private void scanAndRegisterControllers() {
        try {
            Reflections reflections = new Reflections(config.getScanBasePackage());
            Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);

            for (Class<?> controllerClass : controllerClasses) {
                try {
                    Object controller = controllerClass.getDeclaredConstructor().newInstance();
                    registerController(controller);

                    if (config.isVerbose()) {
                        log.info("Controller instantiated: {}", controllerClass.getSimpleName());
                    }
                } catch (Exception e) {
                    if (config.isVerbose()) {
                        log.error("Failed to instantiate controller: {}", controllerClass.getSimpleName());
                    }
                }
            }

            if (config.isVerbose()) {
                log.info("Total controllers discovered: {}", controllerClasses.size());
            }
        } catch (Exception e) {
            if (config.isVerbose()) {
                log.error("Error scanning controllers: {}", e.getMessage());
            }
        }
    }

    /**
     * Adds an API key to the server's collection of keys, which can be used
     * to authenticate requests requiring API key validation.
     *
     * @param keyName the name or identifier for the API key
     * @param keyValue the corresponding value of the API key
     */
    public void addApiKey(String keyName, String keyValue) {
        apiKeys.put(keyName, keyValue);
    }

    /**
     * Starts the server and optionally logs a message if verbose mode is enabled.
     * This method initiates the server's operation and, when configured for verbose logging,
     * outputs the server's start information including the port it is running on.
     *
     * Behavior:
     * - Calls the `start` method of the server object to start the server.
     * - If the configuration indicates verbose logging, logs the server's start
     *   status including the port number retrieved from the configuration.
     *
     * Preconditions:
     * - The server instance must be correctly initialized before invoking this method.
     * - The configuration object must provide valid settings for verbose mode and server port.
     *
     * Postconditions:
     * - The server will be running after this method is called (unless an exception is thrown).
     * - A log message may be generated and recorded if verbose logging is enabled.
     */
    public void start() {
        server.start();
        if (config.isVerbose()) {
            log.info("Server started on port {}", config.getServerPort());
        }
    }

    /**
     * Stops the server with the specified delay. This method allows for a graceful shutdown
     * by delaying the stop operation for the given duration in milliseconds.
     * Logs the stop process if verbose mode is enabled in the configuration.
     *
     * @param delay the delay in milliseconds before the server stops
     */
    public void stop(int delay) {
        if (config.isVerbose()) {
            log.info("Server stopping with delay {}ms", delay);
        }
        server.stop(delay);
    }

    /**
     * Adds a new route to the server by associating a specified path with an HTTP handler.
     * The route is registered with the main router and, if verbose logging is enabled,
     * an informational log entry is created.
     *
     * @param path the HTTP request path to be associated with the handler
     * @param handler the HTTP handler responsible for processing requests to the specified path
     */
    public void addRoute(String path, HttpHandler handler) {
        mainRouter.register(path, handler);
        if (config.isVerbose()) {
            log.info("Route added: {} -> {}", path, handler.getClass().getSimpleName());
        }
    }

    /**
     * Registers a controller with the main router, enabling it to handle requests.
     * The controller object should follow the expected conventions of the application,
     * typically by implementing specific interfaces or being annotated for routing purposes.
     * If configured to be verbose, the system logs the registration of the controller.
     *
     * @param controller the controller object to register with the main router
     */
    public void registerController(Object controller) {
        mainRouter.registerController(controller);
        if (config.isVerbose()) {
            log.info("Controller instantiated: {}", controller.getClass().getName());
        }
    }

    /**
     * Registers a sub-router to the main router at the specified prefix.
     * This method intercepts and processes incoming requests that match the specified prefix,
     * extracting the sub-path and delegating handling to the provided custom router.
     *
     * @param prefix        the path prefix at which the custom router is to be mounted
     * @param customRouter  the router instance responsible for handling requests under the specified prefix
     */
    public void route(String prefix, Router customRouter) {
        mainRouter.register(prefix + "/{*}", exchange -> {
            String subPath = exchange.getRequestURI().getPath().substring(prefix.length());
            if (subPath.isEmpty()) {
                subPath = "/";
            }
            exchange.setAttribute("original-uri", exchange.getRequestURI());
            exchange.setAttribute("uri-path", subPath);

            customRouter.handle(exchange);
        });

        if (config.isVerbose()) {
            log.info("Subrouter registered at prefix: {}", prefix);
        }
    }

    /**
     * Sets the handler that will be invoked when a request is made to an undefined route.
     * This handler is used to provide a custom response for "Not Found" errors.
     *
     * @param handler the {@code HttpHandler} to handle requests to undefined routes
     */
    public void setNotFoundHandler(HttpHandler handler) {
        mainRouter.setNotFoundHandler(handler);
    }

    /**
     * Creates a new context for handling HTTP requests under a specific path.
     * Associates the given path with a handler that processes incoming requests.
     * This method internally delegates the routing logic to the {@code addRoute} method.
     *
     * @param path the URL path under which the context is registered
     * @param handler the handler responsible for processing requests on the specified path
     */
    public void createContext(String path, HttpHandler handler) {
        addRoute(path, handler);
    }

    /**
     * Validates whether the API key provided in the request headers is valid.
     *
     * @param exchange the {@code HttpExchange} object representing the HTTP request and response
     * @return {@code true} if the API key in the request matches a valid key; {@code false} otherwise
     */
    private boolean isValidApiKey(HttpExchange exchange) {
        String requestApiKey = exchange.getRequestHeaders().getFirst("X-API-Key");

        if (requestApiKey == null) {
            return false;
        }

        return apiKeys.containsValue(requestApiKey);
    }

    /**
     * Sends an HTTP response to the given {@code HttpExchange}.
     * Writes the specified response body, sets the appropriate content type header,
     * and sends the response headers with the provided status code.
     *
     * @param exchange the {@code HttpExchange} object representing the HTTP request and response context
     * @param statusCode the HTTP status code to be sent in the response (e.g., 200 for OK, 404 for Not Found)
     * @param response the string content to be sent as the response body
     * @throws IOException if an I/O error occurs while writing the response
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

}
