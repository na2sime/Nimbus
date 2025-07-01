package fr.nassime.nimbus.example;

import fr.nassime.nimbus.NimbusApplication;
import fr.nassime.nimbus.annotations.NimbusApp;

import java.io.IOException;

@NimbusApp
public class SimpleExample {

    public static void main(String[] args) throws IOException {
        NimbusApplication.run(SimpleExample.class, args);
    }

}
