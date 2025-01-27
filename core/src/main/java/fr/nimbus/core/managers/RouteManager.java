package fr.nimbus.core.managers;

import fr.nimbus.api.annotations.Controller;
import fr.nimbus.api.annotations.Route;
import fr.nimbus.api.annotations.UseMiddleware;
import fr.nimbus.api.middleware.MiddlewareResult;
import fr.nimbus.api.middleware.RequestContext;
import fr.nimbus.api.middleware.Middleware;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.*;

public class RouteManager {
    private static final Logger logger = LogManager.getLogger(RouteManager.class);

    private final Map<String, Method> routes = new HashMap<>();
    private final Map<String, Object> controllers = new HashMap<>();
    private final Map<String, List<Middleware>> routeSpecificMiddlewares = new HashMap<>();
    private final MiddlewareManager middlewareManager;

    public RouteManager(MiddlewareManager middlewareManager) {
        this.middlewareManager = middlewareManager;
    }

    public void registerRoutes(Iterable<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                try {
                    Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
                    controllers.put(clazz.getName(), controllerInstance);

                    Controller controllerAnnotation = clazz.getAnnotation(Controller.class);
                    String basePath = controllerAnnotation.path().trim();

                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(Route.class)) {
                            Route routeAnnotation = method.getAnnotation(Route.class);
                            String httpMethod = routeAnnotation.method().toUpperCase();
                            String routePath = basePath + routeAnnotation.path().trim();

                            String routeKey = httpMethod + ":" + routePath;
                            routes.put(routeKey, method);

                            // Vérification et gestion de la nouvelle annotation @UseMiddleware
                            List<Middleware> middlewares = new ArrayList<>();
                            if (method.isAnnotationPresent(UseMiddleware.class)) {
                                UseMiddleware middlewareAnnotation = method.getAnnotation(UseMiddleware.class);
                                for (Class<? extends Middleware> middlewareClass : middlewareAnnotation.value()) {
                                    try {
                                        Middleware middleware = middlewareManager.loadMiddleware(middlewareClass);
                                        middlewares.add(middleware);
                                    } catch (Exception e) {
                                        logger.error("Failed to instantiate middleware: {}", middlewareClass.getName(), e);
                                    }
                                }
                            }

                            routeSpecificMiddlewares.put(routeKey, middlewares);

                            logger.info("Registered route: {} -> {}", routeKey, method.getName());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to register routes for {}", clazz.getName(), e);
                }
            }
        }
    }

    public Object handleRequest(String httpMethod, String path, RequestContext requestContext) throws Exception {
        String routeKey = httpMethod.toUpperCase() + ":" + path;

        Method method = routes.get(routeKey);
        if (method != null) {
            MiddlewareResult result = middlewareManager.executeBefore(requestContext);
            if (result != null && !result.shouldProceed()) {
                return result.getResponse();
            }

            List<Middleware> specificMiddlewares = routeSpecificMiddlewares.getOrDefault(routeKey, Collections.emptyList());
            for (Middleware middleware : specificMiddlewares) {
                result = middleware.before(requestContext);
                if (result != null && !result.shouldProceed()) {
                    return result.getResponse();
                }
            }

            Object controller = controllers.get(method.getDeclaringClass().getName());
            Object response = method.invoke(controller);

            for (Middleware middleware : specificMiddlewares) {
                response = middleware.after(response, requestContext);
            }

            response = middlewareManager.executeAfter(response, requestContext);

            return response;
        }

        return "404 - Not Found";
    }
}