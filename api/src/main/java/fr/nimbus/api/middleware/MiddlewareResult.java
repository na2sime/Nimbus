package fr.nimbus.api.middleware;

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
