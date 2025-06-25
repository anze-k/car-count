package com.anze.CarCounter.util;
import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDeserializer<T> implements Deserializer<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Class<T> targetType;
    private static final String VALUE_DESERIALIZER_TARGET = "com.anze.CarCounter.model.VehiclePassEvent";

    public JsonDeserializer() {}

    @Override
    @SuppressWarnings("unchecked")
    public void configure(Map<String, ?> configs, boolean isKey) {
        try {
            this.targetType = (Class<T>) Class.forName(VALUE_DESERIALIZER_TARGET);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find class for name: " + VALUE_DESERIALIZER_TARGET, e);
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) return null;
        try {
            return objectMapper.readValue(data, targetType);
        } catch (Exception e) {
            throw new SerializationException("Error deserializing JSON message", e);
        }
    }
}