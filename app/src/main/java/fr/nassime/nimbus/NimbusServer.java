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

@Slf4j
public class NimbusServer {

    private final HttpServer server;
    private final Map<String, String> apiKeys;
    private final Router mainRouter;
    private final ServerConfig config;

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

}
