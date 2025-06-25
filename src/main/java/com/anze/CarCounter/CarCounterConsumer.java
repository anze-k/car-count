package com.anze.CarCounter;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.anze.CarCounter.dao.CarCountDao;
import com.anze.CarCounter.model.VehiclePassEvent;
import com.anze.CarCounter.util.DateUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.jimblackler.jsonschemafriend.GenerationException;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.SchemaStore;
import net.jimblackler.jsonschemafriend.ValidationException;
import net.jimblackler.jsonschemafriend.Validator;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class CarCounterConsumer {

    private CarCountDao carCountDao;
    private KafkaConsumer<String, VehiclePassEvent> consumer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CarCounterConsumer() {
        this.carCountDao = new CarCountDao();
    }

    public void startPolling() {
        Properties props = setUpConsumerProperties();
        
        consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Collections.singletonList(Config.TOPIC));

        System.out.println("Consumer started, waiting for messages...");

        try {
            while (true) {
                ConsumerRecords<String, VehiclePassEvent> records = consumer.poll(Duration.ofMillis(100));
                
                for (ConsumerRecord<String, VehiclePassEvent> record : records) {
                    try {
                        VehiclePassEvent carPayload = record.value();
                        validatePayload(carPayload);

                        // Convert timestamp to local date string 
                        String dateInString = DateUtils.timestampToLocalDate(carPayload.getTimestamp());
                        
                        // Process the vehicle and update the count in Redis
                        Boolean newVehicle = carCountDao.insertVehicle(
                            dateInString, carPayload.getVehicleId(), carPayload.getVehicleBrand().name());

                        // If we reach here, the record was processed successfully, so we commit
                        consumer.commitSync();
                        
                        if (newVehicle) {
                            System.out.printf("Vehicle inserted with ID = %s, brand = %s, date = %s\r\n",
                                carPayload.getVehicleId(), carPayload.getVehicleBrand(), dateInString);
                        } else {
                            System.out.printf("Vehicle already counted (ID = %s, brand = %s, date = %s)\r\n",
                                carPayload.getVehicleId(), carPayload.getVehicleBrand(), dateInString);
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing record: " + e.getMessage());
                        continue;
                    }
                }
            }
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        try {
            consumer.wakeup();
            consumer.close();
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    private Properties setUpConsumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Config.GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, com.anze.CarCounter.util.JsonDeserializer.class);
        props.put("json.deserializer.target.type", com.anze.CarCounter.model.VehiclePassEvent.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        return props;
    }

    private void validatePayload(VehiclePassEvent payload) throws ValidationException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(Config.SCHEMA_FILE);) {
            SchemaStore schemaStore = new SchemaStore();
            Schema schema = schemaStore.loadSchema(inputStream);

            Validator validator = new Validator();
            validator.validate(schema, objectMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {}));
        } catch (GenerationException | IOException e) {
            System.err.println("Error loading schema file: " + Config.SCHEMA_FILE);
        }
    }
}

