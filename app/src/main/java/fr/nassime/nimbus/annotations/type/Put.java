package fr.nassime.nimbus.annotations.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a method that handles HTTP PUT requests.
 *
 * This annotation is generally applied to controller methods to specify
 * that the method is responsible for processing PUT requests to the associated
 * endpoint. The optional `path` attribute allows defining the relative URI
 * path for the PUT endpoint.
 *
 * Methods annotated with {@code @Put} can be utilized by routing frameworks or
 * utilities to define and handle HTTP PUT operations in a web server or API context.
 *
 * Attributes:
 * - `path`: Specifies the relative URI path of the PUT endpoint. If left
 *           unspecified, it defaults to an empty string.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Put {
    String path() default "";
}
