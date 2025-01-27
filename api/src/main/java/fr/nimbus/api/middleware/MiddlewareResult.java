package fr.nimbus.api.middleware;

/**
 * Represents the result of a middleware's processing logic, used to determine
 * whether further request processing should proceed or stop immediately.
 *
 * This class is typically used in the context of HTTP request handling frameworks
 * where middleware modules can inspect or modify requests and responses before
 * or after they are processed by the main application logic.
 */
public class MiddlewareResult {
    private final boolean proceed;
    private final Object response; // Used to stop execution with an immediate response (e.g., 403 error).

    public MiddlewareResult(boolean proceed, Object response) {
        this.proceed = proceed;
        this.response = response;
    }

    public boolean shouldProceed() {
        return proceed;
    }

    public Object getResponse() {
        return response;
    }
}
