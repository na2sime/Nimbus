package fr.nimbus.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import fr.nimbus.api.middleware.Middleware;

/**
 * Annotation to specify which middleware(s) to use for a specific route.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UseMiddleware {
    Class<? extends Middleware>[] value(); // Array of middleware classes to use.
}
