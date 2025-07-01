package fr.nassime.nimbus.config;

import lombok.Data;

/**
 * Represents the configuration for a Nimbus server, including server, security,
 * and scanning configurations.
 */
@Data
public class NimbusConfiguration {
    private Server server = new Server();
    private Security security = new Security();
    private Scanning scanning = new Scanning();

    @Data
    public static class Server {
        private int port = 8080;
        private int threadPoolSize = 10;
        private boolean verbose = false;
    }

    @Data
    public static class Security {
        private boolean requireApiKey = false;
        private ApiKeys apiKeys = new ApiKeys();
    }

    @Data
    public static class ApiKeys {
        private String[] keys = new String[]{};
    }

    @Data
    public static class Scanning {
        private boolean autoScanControllers = true;
        private String basePackage = "";
    }
}
