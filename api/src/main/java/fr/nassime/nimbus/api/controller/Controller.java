package fr.nassime.nimbus.api.controller;

import fr.nassime.nimbus.api.routing.Router;

/**
 * Represents a controller that can register routes with a given {@link Router}.
 * This interface is intended to be implemented by classes that define their own routes.
 */
public interface Controller {
    void register(Router router);
}
