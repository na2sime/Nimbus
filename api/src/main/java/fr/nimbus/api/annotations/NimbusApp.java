package fr.nimbus.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to designate a main application class in a Nimbus framework-based application.
 *
 * Classes annotated with @NimbusApp indicate entry points for running a Nimbus application.
 * This annotation facilitates automated configuration of application components such as
 * services, middleware, and routes using class scanning mechanisms.
 *
 * The annotated class must be passed to the framework's run method to initialize the
 * Nimbus Application. If no class is annotated with @NimbusApp, the application will fail to start.
 *
 * This annotation is intended to be used only at the type level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NimbusApp {
}
