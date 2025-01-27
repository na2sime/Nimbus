package fr.nimbus.app;

/**
 * Represents Nimbus configuration options.
 */
public class NimbusConfig {
    private final int port;
    private final boolean corsEnabled;

    private NimbusConfig(int port, boolean corsEnabled) {
        this.port = port;
        this.corsEnabled = corsEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getPort() {
        return port;
    }

    public boolean isCorsEnabled() {
        return corsEnabled;
    }

    /**
     * Builder class for fluent NimbusConfig creation.
     */
    public static class Builder {
        private int port = 8080; // Default port
        private boolean corsEnabled = false;

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withCors(boolean corsEnabled) {
            this.corsEnabled = corsEnabled;
            return this;
        }

        public NimbusConfig build() {
            return new NimbusConfig(port, corsEnabled);
        }
    }
}