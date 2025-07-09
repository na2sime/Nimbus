package fr.nassime.nimbus.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The NimbusApp annotation marks the main application class for a Nimbus-based application.
 * It is used to designate the class containing the main entry point for the Nimbus runtime.
 *
 * This annotation is essential for enabling the Nimbus framework to:
 * - Identify the main application class at runtime.
 * - Initialize application-specific configurations.
 * - Boot up the Nimbus server in the correct context.
 *
 * The marked application class must contain a main method that invokes the NimbusApplication's run method.
 *
 * Usage considerations:
 * - The annotated class must be the entry point of the application.
 * - This annotation is retained at runtime to allow reflective discovery.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NimbusApp {
}
