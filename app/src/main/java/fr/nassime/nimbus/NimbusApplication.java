package fr.nassime.nimbus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import fr.nassime.nimbus.annotations.NimbusApp;
import fr.nassime.nimbus.config.NimbusConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The NimbusApplication class provides the entry point for starting a Nimbus-based server application.
 * It is primarily responsible for bootstrapping and running the Nimbus server,
 * while loading necessary configurations and managing application-specific settings.
 *
 * Key Features:
 * - Validates that the provided main class is annotated with the @NimbusApp annotation.
 * - Loads the Nimbus configuration from a YAML file or falls back to default configurations.
 * - Creates and configures a NimbusServer instance using the loaded configuration.
 * - Optionally adds API keys for securing access if required by the configuration.
 *
 * Important Considerations:
 * - The main class must be annotated with @NimbusApp for the application to run.
 * - If the configuration file `nimbus.yaml` is not present or invalid, default settings are applied.
 * - The `run` method starts the Nimbus server and blocks the main thread as long as the server is running.
 *
 * Thread Safety:
 * This class is designed to be invoked as part of the application startup process
 * and is not thread-safe for concurrent modifications.
 *
 * Exceptions:
 * - Throws IllegalArgumentException if the main class is not annotated with @NimbusApp.
 * - Throws IOException if server initialization encounters an IO-related failure.
 */
@Slf4j
public class NimbusApplication {

    /**
     * Starts a Nimbus application server based on the provided main class and arguments.
     * The provided main class must be annotated with {@code @NimbusApp}.
     * Configures the server with parameters loaded from the Nimbus configuration,
     * such as port, thread pool size, API key requirements, and controller scanning options.
     *
     * @param mainClass the main class of the application, which must be annotated with {@code @NimbusApp}
     * @param args optional command-line arguments
     * @return an instance of {@code NimbusServer} representing the running server
     * @throws IllegalArgumentException if the given {@code mainClass} is not annotated with {@code @NimbusApp}
     * @throws IOException if the server fails to start or encounters an I/O error
     */
    public static NimbusServer run(Class<?> mainClass, String... args) throws IOException {
        if (!mainClass.isAnnotationPresent(NimbusApp.class)) {
            throw new IllegalArgumentException("The main class must be annotated with @NimbusApp");
        }

        NimbusConfiguration config = loadConfiguration();
        String effectiveBasePackage = config.getScanning().getBasePackage().isEmpty()
            ? mainClass.getPackage().getName()
            : config.getScanning().getBasePackage();

        NimbusServer server = NimbusServer.builder()
            .port(config.getServer().getPort())
            .threadPoolSize(config.getServer().getThreadPoolSize())
            .apiKeyRequired(config.getSecurity().isRequireApiKey())
            .autoScanControllers(config.getScanning().isAutoScanControllers())
            .basePackage(effectiveBasePackage)
            .verbose(config.getServer().isVerbose())
            .build();

        server.start();

        if (config.getSecurity().isRequireApiKey()) {
            for (String key : config.getSecurity().getApiKeys().getKeys()) {
                server.addApiKey("default", key);
            }
        }

        return server;
    }

    /**
     * Loads the configuration for the Nimbus server from a YAML file.
     * If the file `nimbus.yaml` exists in the application's root directory,
     * it attempts to parse the content and return a `NimbusConfiguration` object.
     * In case of an error or if the file does not exist, a default configuration is returned.
     *
     * @return A `NimbusConfiguration` object representing the server's configuration.
     *         If the file is not found or an error occurs during reading,
     *         the default configuration is returned.
     */
    private static NimbusConfiguration loadConfiguration() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Path configPath = Paths.get("nimbus.yaml");

        if (Files.exists(configPath)) {
            try {
                log.info("Loading configuration from nimbus.yaml");
                return mapper.readValue(new File("nimbus.yaml"), NimbusConfiguration.class);
            } catch (IOException e) {
                log.warn("Impossible to load nimbus.yaml, using default configuration", e);
            }
        }

        return new NimbusConfiguration();
    }
}
