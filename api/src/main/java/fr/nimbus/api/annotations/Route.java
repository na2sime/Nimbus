package fr.nimbus.api.annotations;

import java.lang.annotation.*;

/**
 * Annotation to define a route mapping for a method in a controller class.
 *
 * This annotation is used to specify the HTTP method and the path for a route
 * that will be handled by the annotated method. It is typically applied to
 * controller methods within classes annotated with {@code @Controller}.
 *
 * The {@code method} attribute specifies the HTTP method (e.g., GET, POST, PUT, DELETE)
 * that the route responds to, and the {@code path} attribute defines the relative path
 * that will be handled by the method.
 *
 * Methods annotated with {@code @Route} are detected and registered in the route
 * management system, associating the specified HTTP method and path combination
 * with the annotated method for request handling.
 *
 * Attributes:
 * - {@code method}: The HTTP method that defines the type of request the route will handle.
 * - {@code path}: The relative path that specifies the route's endpoint.
 *
 * Example usage of this annotation would typically involve associating HTTP routes
 * with specific methods in a scanned controller, enabling routing and dispatch.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {
    String method();

    String path();
}
