package fr.nassime.nimbus.example;

import fr.nassime.nimbus.NimbusApplication;
import fr.nassime.nimbus.api.annotations.NimbusApp;

@NimbusApp
public class SimpleExample {
    /**
     * This is a simple example of a Nimbus application.
     * It demonstrates how to create a basic Nimbus application with no additional features.
     * To run this example, use the command: `java -jar example.jar`
     */
    public static void main(String[] args) {
        NimbusApplication.run(SimpleExample.class, args);
    }
}

