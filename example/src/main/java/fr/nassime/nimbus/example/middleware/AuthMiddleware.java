
package fr.nassime.nimbus.example.middleware;

import com.sun.net.httpserver.HttpExchange;
import fr.nassime.nimbus.middleware.Middleware;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class AuthMiddleware implements Middleware {
    @Override
    public boolean handle(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Auth failed");
            sendUnauthorizedResponse(exchange);
            return false;
        }

        String token = authHeader.substring(7);

        if (!isValidToken(token)) {
            log.warn("Invalid Token: {}", token);
            sendUnauthorizedResponse(exchange);
            return false;
        }

        return true;
    }

    private boolean isValidToken(String token) {
        // Here you would implement your token validation logic.
        return !token.isEmpty();
    }

    private void sendUnauthorizedResponse(HttpExchange exchange) throws IOException {
        String response = "Unauthorized: Token is missing or invalid";
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(401, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }
}
