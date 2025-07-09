package fr.nassime.nimbus.controller;

import fr.nassime.nimbus.routing.Router;

/**
 * Represents a controller that can register routers to handle routing logic.
 */
public interface Controller {
    /**
     * Registers a router to handle routing logic within the controller.
     *
     * @param router the Router instance to be registered with the controller
     */
    void register(Router router);
}
