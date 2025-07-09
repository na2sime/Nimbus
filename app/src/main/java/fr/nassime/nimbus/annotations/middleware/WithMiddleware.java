package fr.nassime.nimbus.annotations.middleware;

import fr.nassime.nimbus.middleware.Middleware;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(WithMiddlewares.class)
public @interface WithMiddleware {
    Class<? extends Middleware> value();
}
