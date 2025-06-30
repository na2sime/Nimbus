package fr.nassime.nimbus.api.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;

class JsonUtilsTest {

    static class TestObject {
        private String name;
        private int age;

        public TestObject() {
        }

        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    @Test
    void shouldSerializeObjectToJson() throws JsonProcessingException {
        // Given
        TestObject testObject = new TestObject("John", 30);

        // When
        String json = JsonUtils.toJson(testObject);

        // Then
        assertThat(json).contains("\"name\":\"John\"");
        assertThat(json).contains("\"age\":30");
    }

    @Test
    void shouldDeserializeJsonToObject() throws JsonProcessingException {
        // Given
        String json = "{\"name\":\"Jane\",\"age\":25}";

        // When
        TestObject testObject = JsonUtils.fromJson(json, TestObject.class);

        // Then
        assertThat(testObject.getName()).isEqualTo("Jane");
        assertThat(testObject.getAge()).isEqualTo(25);
    }

    @Test
    void shouldHandleInvalidJson() {
        // Given
        String invalidJson = "{name:\"Invalid\"}";

        // When & Then
        assertThrows(JsonProcessingException.class, () -> {
            JsonUtils.fromJson(invalidJson, TestObject.class);
        });
    }
}
