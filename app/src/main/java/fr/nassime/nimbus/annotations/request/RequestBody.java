package fr.nassime.nimbus.annotations.request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a method parameter should be bound to the body of the HTTP request.
 *
 * The @RequestBody annotation is typically used for extracting the request payload in formats such
 * as JSON or XML and binding it to an object parameter of the handler method. This is particularly
 * useful in RESTful APIs for handling HTTP methods like POST, PUT, or PATCH where data is sent in
 * the request body.
 *
 * By default, the request body is deserialized into an instance of the target class specified
 * by the method parameter type. Custom deserialization rules or converters may be applied to transform
 * the request payload into the target object.
 *
 * This annotation can only be applied to method parameters and assumes that the request body is
 * consumable, such as in JSON, XML, or plain text formats.
 *
 * Typical usage includes scenarios where a method parameter represents a resource entity that is
 * being created or modified based on the submitted request data.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {
}
