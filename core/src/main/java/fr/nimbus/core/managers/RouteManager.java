package fr.nimbus.core.managers;

import fr.nimbus.api.annotations.Controller;
import fr.nimbus.api.annotations.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RouteManager {
    private static final Logger logger = LogManager.getLogger(RouteManager.class);

    // Map to store registered routes: HTTP_METHOD:URL --> Method (controller method)
    private final Map<String, Method> routes = new HashMap<>();
    // Map to store the corresponding controller instances
    private final Map<String, Object> controllers = new HashMap<>();

    /**
     * Registers the routes and controllers from the given classes.
     */
    public void registerRoutes(Iterable<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                try {
                    // Instantiate the controller and save it in a map
                    Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
                    controllers.put(clazz.getName(), controllerInstance);

                    // Retrieve the base path from @Controller
                    Controller controllerAnnotation = clazz.getAnnotation(Controller.class);
                    String basePath = controllerAnnotation.path();

                    // Register the routes for each method with @Route
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(Route.class)) {
                            Route routeAnnotation = method.getAnnotation(Route.class);
                            String httpMethod = routeAnnotation.method().toUpperCase(); // e.g., GET
                            String routePath = basePath + routeAnnotation.path().trim(); // Full path

                            // Construct a unique route key: HTTP_METHOD:/path
                            String routeKey = httpMethod + ":" + routePath;

                            // Register the route
                            routes.put(routeKey, method);

                            logger.info("Registered route: {} -> {}", routeKey, method.getName());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to register routes for {}", clazz.getName(), e);
                }
            }
        }
    }

    /**
     * Handles an incoming HTTP request and routes it to the appropriate controller method.
     */
    public Object handleRequest(String httpMethod, String path) throws Exception {
        String routeKey = httpMethod.toUpperCase() + ":" + path;

        Method method = routes.get(routeKey);
        if (method != null) {
            // Get the controller instance for this route
            Object controller = controllers.get(method.getDeclaringClass().getName());
            // Invoke the controller method and return the result
            return method.invoke(controller);
        }

        // Return null if no route matches (404)
        return null;
    }
}