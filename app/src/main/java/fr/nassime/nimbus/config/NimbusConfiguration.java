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

    /**
     * Represents the configuration settings for a server,
     * including port, thread pool size, and verbosity.
     */
    @Data
    public static class Server {
        private int port = 8080;
        private int threadPoolSize = 10;
        private boolean verbose = true;
    }

    /**
     * Represents the security configuration for a Nimbus server,
     * including settings for API key requirement and API key definitions.
     *
     * The security configuration determines whether an API key is required
     * for accessing the server and provides a mechanism to manage the keys.
     *
     * Fields:
     * - requireApiKey: Indicates whether API key validation is enforced.
     * - apiKeys: Stores the list of API keys used for authorization.
     */
    @Data
    public static class Security {
        private boolean requireApiKey = false;
        private ApiKeys apiKeys = new ApiKeys();
    }

    /**
     * Represents a collection of API keys used for security and authorization purposes.
     *
     * This class is part of the security configuration for the `NimbusConfiguration`.
     * It provides a mechanism to define and manage API keys that can be used
     * for authenticating requests to the server.
     *
     * Fields:
     * - keys: An array of strings representing the API keys used for authorization.
     */
    @Data
    public static class ApiKeys {
        private String[] keys = new String[]{};
    }

    /**
     * Represents the scanning configuration for controllers within the Nimbus server.
     *
     * This class provides settings for automatic controller detection and the base package
     * where controllers should be scanned. It enables developers to configure whether the
     * system automatically scans for controllers and to specify the package structure for scanning.
     *
     * Fields:
     * - autoScanControllers: Indicates whether automatic scanning of controllers is enabled.
     * - basePackage: Specifies the base package to scan for controllers.
     */
    @Data
    public static class Scanning {
        private boolean autoScanControllers = true;
        private String basePackage = "";
    }
}
