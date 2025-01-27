package fr.nimbus.core.managers;

import fr.nimbus.api.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceManager {
    private static final Logger logger = LogManager.getLogger(ServiceManager.class);

    private final Map<String, Object> services = new ConcurrentHashMap<>();

    public void registerServices(List<Class<?>> serviceClasses) {
        for (Class<?> clazz : serviceClasses) {
            if (clazz.isAnnotationPresent(Service.class)) {
                registerService(clazz);
            }
        }
    }

    private void registerService(Class<?> clazz) {
        Service serviceAnnotation = clazz.getAnnotation(Service.class);
        String serviceName = serviceAnnotation.name();
        Class<?>[] dependencies = serviceAnnotation.dependencies();

        List<Object> resolvedDependencies = new ArrayList<>();
        for (Class<?> dependency : dependencies) {
            Object service = services.get(dependency.getSimpleName());
            if (service == null) {
                logger.error("Missing dependency: {}", dependency.getSimpleName());
                throw new IllegalStateException("Missing dependency: " + dependency.getSimpleName());
            }
            resolvedDependencies.add(service);
        }

        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            logger.info("Service registered: {}", serviceName);
            services.put(serviceName, instance);
        } catch (Exception e) {
            logger.error("Failed to instantiate service: {}", serviceName, e);
            throw new IllegalStateException("Failed to instantiate service: " + serviceName, e);
        }
    }

    public Object getService(String name) {
        return services.get(name);
    }
}