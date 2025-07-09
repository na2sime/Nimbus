package fr.nassime.nimbus.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Utility class for working with JSON data using the Jackson library.
 * This class provides methods for converting objects to JSON strings and
 * vice versa, while ensuring proper configuration of the Jackson ObjectMapper.
 * It is designed to simplify JSON serialization and deserialization tasks.
 */
public class JsonUtils {
    /**
     * A preconfigured {@link ObjectMapper} instance used for JSON serialization and deserialization.
     * This ObjectMapper is set up to:
     * - Automatically find and register all Jackson modules available on the classpath.
     * - Ignore unknown properties during deserialization to ensure compatibility with incomplete or
     *   evolving JSON payloads.
     *
     * This static instance is used throughout the containing utility class to provide consistent
     * behavior and configuration for JSON processing tasks.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    static {
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Converts the given object to its JSON string representation using the Jackson library.
     * This method leverages a preconfigured ObjectMapper instance to ensure consistent
     * serialization behavior.
     *
     * @param object the object to be converted to JSON. Must not be null.
     * @return the JSON string representation of the given object.
     * @throws JsonProcessingException if an error occurs during the conversion to JSON.
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(object);
    }

    /**
     * Deserializes a JSON string into an object of the specified type.
     *
     * @param <T>   the type of the object to deserialize the JSON into
     * @param json  the JSON string to be deserialized
     * @param clazz the {@link Class} object of the type to deserialize the JSON into
     * @return the deserialized object of type {@code T}
     * @throws JsonProcessingException if parsing the JSON string fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(json, clazz);
    }

    /**
     * Provides access to the shared {@link ObjectMapper} instance configured for JSON processing.
     * The returned ObjectMapper is pre-configured to handle common serialization and deserialization
     * scenarios, such as ignoring unknown properties and suppressing errors for empty beans.
     *
     * @return the pre-configured {@link ObjectMapper} instance for JSON serialization and deserialization.
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
