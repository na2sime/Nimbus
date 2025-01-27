package fr.nimbus.api.middleware;

import io.netty.handler.codec.http.FullHttpRequest;

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
