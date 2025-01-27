package fr.nimbus.api.annotations;

import java.lang.annotation.*;

/**
 * Annotation to mark a class as a service in a service management system.
 * This annotation is used to define a service by specifying its name
 * and an optional list of dependencies. These dependencies will be
 * resolved during the registration process of the service.
 *
 * The `name` attribute specifies the unique identifier for the service.
 * The `dependencies` attribute defines the classes required for the service
 * to function and will be resolved at runtime by the service manager.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {
    String name();

    Class<?>[] dependencies() default {};
}
