package fr.nimbus.api.annotations;

import java.lang.annotation.*;

/**
 * Annotation to designate a class as a controller for handling HTTP requests.
 * The {@code path} attribute specifies the base path for all routes defined
 * within the annotated controller class.
 *
 * Classes annotated with {@code Controller} are typically scanned and
 * registered by a route management system, enabling their routes and
 * associated request handling methods to be utilized during application runtime.
 *
 * Each controller may define multiple methods annotated with routing annotations
 * (e.g., {@code @Route}) to handle specific HTTP methods and paths corresponding
 * to the controller's base path. Controllers can also make use of middleware
 * annotations to intercept, modify, or block requests and responses.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {
    String path();
}
