package fr.nimbus.api.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {
    String name();

    Class<?>[] dependencies() default {};
}
