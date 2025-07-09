package fr.nassime.nimbus.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Controller annotation is used to mark a class as a web controller in the application.
 * Controllers are responsible for handling incoming HTTP requests and providing appropriate
 * responses. This annotation also allows specifying a base path for all endpoints
 * defined within the controller.
 *
 * Attributes:
 * - `path`: Defines the base URI path for the controller. Default is an empty string,
 *   indicating that no base path is specified.
 *
 * This annotation should be used on classes and is retained at runtime to allow
 * dynamic processing.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
    String path() default "";
}
