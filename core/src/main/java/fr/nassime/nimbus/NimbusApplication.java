package fr.nassime.nimbus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import fr.nassime.nimbus.api.annotations.NimbusApp;
import fr.nassime.nimbus.config.NimbusConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The NimbusApplication class serves as the entry point to initialize and run a NimbusServer
 * application with specific configurations. This class is responsible for validating the
 * main application class, loading configuration from a YAML file, and starting the server
 * with the specified settings.
 *
 * Key functionality includes:
 * - Ensuring the main application class is annotated with @NimbusApp.
 * - Loading server and application configuration from a `nimbus.yaml` file or default settings.
 * - Creating and starting a configured NimbusServer instance.
 * - Managing API key setup if security settings require it.
 */
@Slf4j
public class NimbusApplication {

    public static NimbusServer run(Class<?> mainClass, String... args) {
        if (!mainClass.isAnnotationPresent(NimbusApp.class)) {
            throw new IllegalArgumentException("The main class must be annotated with @NimbusApp");
        }

        NimbusConfiguration config = loadConfiguration();
        try {
            NimbusServer server = NimbusServer.builder()
                .port(config.getServer().getPort())
                .threadPoolSize(config.getServer().getThreadPoolSize())
                .requireApiKey(config.getSecurity().isRequireApiKey())
                .autoScanControllers(config.getScanning().isAutoScanControllers())
                .basePackage(config.getScanning().getBasePackage().isEmpty()
                    ? mainClass.getPackage().getName()
                    : config.getScanning().getBasePackage())
                .verbose(config.getServer().isVerbose())
                .build();

            server.start();

            if (config.getSecurity().isRequireApiKey()) {
                for (String key : config.getSecurity().getApiKeys().getKeys()) {
                    server.addApiKey("default", key);
                }
            }

            return server;

        } catch (IOException e) {
            throw new RuntimeException("Error while starting Nimbus server", e);
        }
    }


    private static NimbusConfiguration loadConfiguration() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Path configPath = Paths.get("nimbus.yaml");

        if (Files.exists(configPath)) {
            try {
                return mapper.readValue(new File("nimbus.yaml"), NimbusConfiguration.class);
            } catch (IOException e) {
                log.warn("Impossible to load nimbus.yaml, using default configuration", e);
            }
        }

        return new NimbusConfiguration();
    }
}
