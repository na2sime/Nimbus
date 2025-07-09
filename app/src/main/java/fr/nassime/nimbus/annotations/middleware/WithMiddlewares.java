package fr.nassime.nimbus.annotations.middleware;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to group multiple {@link WithMiddleware} annotations that are applied
 * to a class or a method. This annotation allows middleware components to be defined
 * and executed in the specified order to handle HTTP requests.
 *
 * This annotation functions as a container for multiple {@link WithMiddleware} annotations,
 * which are evaluated sequentially when applied to the target class or method.
 *
 * Use this annotation:
 * - To configure multiple middleware components at once for a single target.
 * - To combine multiple {@link WithMiddleware} entries more efficiently.
 *
 * Middleware components are executed in the order provided within the `value` array,
 * allowing fine-grained control over the execution pipeline for tasks such as
 * authentication, authorization, request logging, or input transformation.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WithMiddlewares {
    WithMiddleware[] value();
}
