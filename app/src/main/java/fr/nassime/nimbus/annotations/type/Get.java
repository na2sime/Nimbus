package fr.nassime.nimbus.annotations.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a method that handles HTTP GET requests.
 *
 * This annotation is typically applied to controller methods to specify
 * that the method is responsible for processing GET requests to a corresponding
 * endpoint. The optional `path` attribute allows specifying the endpoint's
 * relative path to be routed.
 *
 * Methods annotated with {@code @Get} can be leveraged by frameworks or route
 * registration utilities to define and map HTTP GET operations on a server.
 *
 * Attributes:
 * - `path`: Defines the relative URI path of the GET endpoint. If not
 *           specified, the default is an empty string.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Get {
    String path() default "";
}
