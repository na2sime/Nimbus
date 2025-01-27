package fr.nimbus.api.route;

import fr.nimbus.api.middleware.Middleware;
import fr.nimbus.api.middleware.RequestContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents a route definition in a web application framework.
 * This class specifies the HTTP method, path, controller, and associated middleware
 * for handling a specific route in the application.
 */
public class RouteDefinition {
    private final String method;
    private final String path;
    private final Method handlerMethod;
    private final Object controller;
    private final List<Middleware> middlewares;

    public RouteDefinition(String method, String path, Method handlerMethod, Object controller, List<Middleware> middlewares) {
        this.method = method;
        this.path = path;
        this.handlerMethod = handlerMethod;
        this.controller = controller;
        this.middlewares = middlewares;
    }

    public boolean matches(String method, String path) {
        return this.method.equalsIgnoreCase(method) && this.path.equalsIgnoreCase(path);
    }

    public Object invoke(RequestContext requestContext) throws InvocationTargetException, IllegalAccessException {
        return handlerMethod.invoke(controller, requestContext);
    }

    public List<Middleware> getMiddlewares() {
        return middlewares;
    }
}
