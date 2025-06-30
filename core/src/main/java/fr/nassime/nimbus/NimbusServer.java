package fr.nassime.nimbus;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import fr.nassime.nimbus.api.annotations.Controller;
import fr.nassime.nimbus.api.routing.Router;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * NimbusServer is a lightweight HTTP server designed to handle simplified routing,
 * controller registration, and API key-based authentication. It allows configuration
 * through a builder pattern and supports extensibility with routers, routes, and controllers.
 *
 * Thread management and other server behaviors are configurable via the ServerConfig object.
 */
@Slf4j
public class NimbusServer {

    private final HttpServer server;
    private final Map<String, String> apiKeys;
    private final Router mainRouter;
    private final ServerConfig config;

    private NimbusServer(Builder builder) throws IOException {
        this.config = builder.config;
        this.server = HttpServer.create(new InetSocketAddress(config.getServerPort()), config.getServerBacklog());
        this.server.setExecutor(config.getServerExecutor());
        this.apiKeys = new HashMap<>();

        this.mainRouter = new Router() {
            @Override
            protected void initRoutes() {
                // Default route for root path
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

    private void scanAndRegisterControllers() {
        try {
            Reflections reflections = new Reflections(config.getScanBasePackage());
            Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);

            for (Class<?> controllerClass : controllerClasses) {
                try {
                    Object controller = controllerClass.getDeclaredConstructor().newInstance();
                    registerController(controller);

                    if (config.isVerbose()) {
                        log.info("Controller instantiated: {}", controllerClass.getName());
                    }
                } catch (Exception e) {
                    if (config.isVerbose()) {
                        log.error("Failed to instantiate controller: {}", controllerClass.getName());
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

    public void addApiKey(String keyName, String keyValue) {
        apiKeys.put(keyName, keyValue);
    }

    public void start() {
        server.start();
        if (config.isVerbose()) {
            log.info("Server started on port {}", config.getServerPort());
        }
    }

    public void stop(int delay) {
        if (config.isVerbose()) {
            log.info("Server stopping with delay {}ms", delay);
        }
        server.stop(delay);
    }

    public void addRoute(String path, HttpHandler handler) {
        mainRouter.register(path, handler);
        if (config.isVerbose()) {
            log.info("Route added: {} -> {}", path, handler.getClass().getName());
        }
    }

    public void registerController(Object controller) {
        mainRouter.registerController(controller);
        if (config.isVerbose()) {
            log.info("Controller instantiated: {}", controller.getClass().getName());
        }
    }

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

    public void setNotFoundHandler(HttpHandler handler) {
        mainRouter.setNotFoundHandler(handler);
    }

    public void createContext(String path, HttpHandler handler) {
        addRoute(path, handler);
    }

    private boolean isValidApiKey(HttpExchange exchange) {
        String requestApiKey = exchange.getRequestHeaders().getFirst("X-API-Key");

        if (requestApiKey == null) {
            return false;
        }

        return apiKeys.containsValue(requestApiKey);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    @Getter
    public static class ServerConfig {
        private final int serverPort;
        private final int serverBacklog;
        private final Executor serverExecutor;
        private final boolean isApiKeyRequired;
        private final boolean isAutoScanControllers;
        private final String scanBasePackage;
        private final boolean isVerbose;

        private ServerConfig(Builder builder) {
            this.serverPort = builder.port;
            this.serverBacklog = builder.backlog;
            this.serverExecutor = builder.executor;
            this.isApiKeyRequired = builder.apiKeyRequired;
            this.isAutoScanControllers = builder.autoScanControllers;
            this.scanBasePackage = builder.basePackage;
            this.isVerbose = builder.verbose;
        }
    }

    public static class Builder {
        private int port = 8080;
        private int backlog;
        private Executor executor = Executors.newFixedThreadPool(10);
        private boolean apiKeyRequired;
        private boolean autoScanControllers;
        private String basePackage = "";
        private boolean verbose;
        private ServerConfig config;

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder backlog(int backlog) {
            this.backlog = backlog;
            return this;
        }

        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public Builder threadPoolSize(int size) {
            this.executor = Executors.newFixedThreadPool(size);
            return this;
        }

        public Builder requireApiKey(boolean required) {
            this.apiKeyRequired = required;
            return this;
        }

        public Builder autoScanControllers(boolean autoScan) {
            this.autoScanControllers = autoScan;
            return this;
        }

        public Builder basePackage(String basePackage) {
            this.basePackage = basePackage;
            return this;
        }

        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public NimbusServer build() throws IOException {
            this.config = new ServerConfig(this);
            return new NimbusServer(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
