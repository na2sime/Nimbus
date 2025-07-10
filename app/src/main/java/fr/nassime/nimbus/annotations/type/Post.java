package fr.nassime.nimbus.annotations.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a method that handles HTTP POST requests.
 *
 * This annotation is typically applied to controller methods to indicate
 * that the method is responsible for processing POST requests to a specified
 * endpoint. The optional `path` attribute allows defining the relative URI
 * path for the POST endpoint.
 *
 * Methods annotated with {@code @Post} can be used in conjunction with routing
 * frameworks or utilities to define and handle HTTP POST operations for incoming
 * requests in a web server or API context.
 *
 * Attributes:
 * - `path`: Defines the relative URI path of the POST endpoint. If not
 *           specified, the default is an empty string.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Post {
    String path() default "";
}
