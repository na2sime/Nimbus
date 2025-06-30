package fr.nassime.nimbus.api.routing;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import fr.nassime.nimbus.api.annotations.Controller;
import fr.nassime.nimbus.api.annotations.Get;
import fr.nassime.nimbus.api.annotations.PathVariable;
import fr.nassime.nimbus.api.http.ResponseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RouterTest {

    @Mock
    private HttpExchange exchange;

    @Mock
    private Headers headers;

    @Mock
    private OutputStream outputStream;

    private TestRouter router;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        router = new TestRouter();

        // Configuration du mock HttpExchange
        when(exchange.getRequestURI()).thenReturn(URI.create("/test/123"));
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(outputStream);

        // Configuration pour les paramètres de chemin
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", "123");
        when(exchange.getAttribute("pathParams")).thenReturn(pathParams);
    }

    @Test
    void shouldRegisterRouteAndHandleRequest() throws IOException {
        // Given
        router.register("/test/{id}", exchange -> {
            String id = router.getPathParam(exchange, "id");
            assertThat(id).isEqualTo("123");
        });

        // When
        router.handle(exchange);

        // Then
        verify(exchange).setAttribute(eq("pathParams"), eq(Map.of("id", "123")));
    }

    @Test
    void shouldRegisterControllerAndHandleRequest() throws IOException {
        // Given
        TestController controller = new TestController();
        router.registerController(controller);
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/users/456"));
        when(exchange.getRequestMethod()).thenReturn("GET");

        // Set up path params for this test
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", "456");
        when(exchange.getAttribute("pathParams")).thenReturn(pathParams);

        // When
        router.handle(exchange);

        // Then
        verify(exchange).setAttribute(eq("pathParams"), eq(Map.of("id", "456")));
        verify(headers).set(eq("Content-Type"), eq("application/json; charset=UTF-8"));
        verify(exchange).sendResponseHeaders(eq(200), anyLong());
    }

    private static class TestRouter extends Router {
        @Override
        protected void initRoutes() {
            // Pas de routes par défaut
        }
    }

    @Controller(path = "/api/users")
    private static class TestController {
        @Get(path = "/{id}")
        public ResponseEntity<String> getUser(@PathVariable("id") String id) {
            return ResponseEntity.ok("User with ID: " + id);
        }
    }
}
