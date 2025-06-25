package com.anze.CarCounter;

public class Config {
    public static final String SCHEMA_FILE = "CarSchema.json";
    public static final String REDIS_HOST = "localhost";
    public static final int REDIS_PORT = 6379;
    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String GROUP_ID = "test-consumer-group";
    public static final String TOPIC = "car-topic";
}
