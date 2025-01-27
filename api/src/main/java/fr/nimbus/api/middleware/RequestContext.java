package fr.nimbus.api.middleware;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Represents the HTTP request context, encapsulating details of the current request such as
 * the HTTP method, URL path, and headers. This class provides utility methods for accessing
 * request attributes and is typically used within middleware, route definitions, and controllers
 * to handle and inspect the incoming HTTP request data.
 */
public class RequestContext {
    private final FullHttpRequest request;

    public RequestContext(FullHttpRequest request) {
        this.request = request;
    }

    // Expose request details (headers, method, path, etc.)
    public String getMethod() {
        return request.method().name();
    }

    public String getPath() {
        return request.uri();
    }

    public String getHeader(String name) {
        return request.headers().get(name);
    }
}
