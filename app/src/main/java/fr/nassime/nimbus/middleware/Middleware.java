package fr.nassime.nimbus.middleware;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

@FunctionalInterface
public interface Middleware {
    boolean handle(HttpExchange exchange) throws IOException;
}

