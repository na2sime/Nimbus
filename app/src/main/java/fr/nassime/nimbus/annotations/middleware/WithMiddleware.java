package fr.nassime.nimbus.annotations.middleware;

import fr.nassime.nimbus.middleware.Middleware;
import java.lang.annotation.*;

/**
 * Annotation used to specify a middleware component that should be applied
 * to a class or a method during HTTP request handling. This annotation allows
 * class-level or method-level configuration of middleware for specific
 * routes or controllers.
 *
 * Middleware components implementing the {@link Middleware} interface
 * can be specified by providing their class type to this annotation.
 * Middleware annotated with this annotation will be executed in the
 * order they are defined.
 *
 * This annotation is repeatable, allowing multiple middleware to be
 * applied. When multiple instances are present, they are grouped using
 * the {@link WithMiddlewares} annotation.
 *
 * Usage:
 * - Annotate a controller class or method with this annotation and specify
 *   the class type of the middleware to apply.
 * - Middleware execution occurs before accessing the route or controller's
 *   logic, typically to handle tasks such as authentication, logging, or
 *   request transformation.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(WithMiddlewares.class)
public @interface WithMiddleware {
    Class<? extends Middleware> value();
}
