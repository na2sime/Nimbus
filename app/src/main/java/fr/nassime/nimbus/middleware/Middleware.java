package fr.nassime.nimbus.middleware;

import com.sun.net.httpserver.HttpExchange;
import fr.nassime.nimbus.annotations.middleware.WithMiddleware;

import java.io.IOException;

/**
 * Represents a middleware component that intercepts HTTP requests and performs
 * specific operations, such as authentication, logging, or request preprocessing,
 * before the request is forwarded to the next stage of handling.
 *
 * Implementations of this interface must provide the `handle` method, which defines
 * the logic to be executed during middleware processing. The `handle` method can
 * inspect and modify the incoming {@link HttpExchange} object and determines whether
 * further processing of the request should continue.
 *
 * The middleware chain proceeds if the `handle` method returns `true`. If it returns
 * `false`, further processing is halted, and the response is sent back to the client.
 *
 * This interface is often used in conjunction with annotations such as {@link WithMiddleware}
 * to apply middleware at class or method level in a router or controller structure,
 * ensuring modular and reusable request handling logic.
 *
 *
 * Functional Interface:
 * This interface is marked as a functional interface, allowing its implementation
 * to be expressed as a lambda function or method reference for succinctness.
 *
 * Thread-Safety:
 * Implementations of this interface should ensure thread-safe operations, as HTTP requests
 * may be processed concurrently.
 */
@FunctionalInterface
public interface Middleware {
    /**
     * Handles an HTTP exchange during middleware processing by performing specific
     * operations on the request or response, such as validation, transformation, or
     * execution of custom logic.
     *
     * @param exchange the {@link HttpExchange} object representing the HTTP request
     *                 and response pair to be processed. It provides access to
     *                 request details, headers, input/output streams, and allows
     *                 interaction with the HTTP response.
     * @return {@code true} if the middleware processing should continue to the next
     *         middleware or the final route logic; {@code false} if further processing
     *         is halted and the response is sent immediately to the client.
     * @throws IOException if an input/output error occurs during processing.
     */
    boolean handle(HttpExchange exchange) throws IOException;
}
