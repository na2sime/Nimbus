package fr.nassime.nimbus.annotations.request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a method parameter should be bound to a specific
 * value extracted from a URI's path variable. Path variables are placeholders
 * present in the URI path, typically enclosed within `{}` brackets, and serve
 * as dynamic segments within the URI.
 *
 * For example, in a route handler for `/users/{id}`, the value of the `id`
 * path segment in the URI will be injected into the method parameter annotated
 * with @PathVariable("id").
 *
 * If a value is not specified in the annotation, the parameter name will
 * be used to resolve the path variable by default.
 *
 * This annotation can only be applied to method parameters and relies on
 * runtime reflection to extract and bind the path segment value.
 *
 * Applicable scenarios include cases where dynamic or user-specific segments
 * are embedded in API endpoint paths and need to be captured during request
 * handling.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {
    String value() default "";
}
