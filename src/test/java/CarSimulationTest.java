import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.anze.CarCounter.CarCounterConsumer;
import com.anze.CarCounter.dao.CarCountDao;
import com.anze.CarCounter.model.VehiclePassEvent;
import com.anze.CarCounter.model.VehiclePassEvent.VehicleBrand;
import com.anze.CarCounter.util.JsonSerializer;

import static org.awaitility.Awaitility.await;

public class CarSimulationTest {
    private final static String TOPIC = "car-topic";
    private final static String BOOTSTRAP_SERVERS = "localhost:9092";
    private final static ArrayList<VehiclePassEvent> CarList = new ArrayList<VehiclePassEvent>() {{
        add(new VehiclePassEvent(6579831L, "66974ae1a0a5b", VehicleBrand.VOLKSWAGEN, 1721191660000L));
        add(new VehiclePassEvent(6579832L, "66974ae1a0a5c", VehicleBrand.BYD, 1721191660000L));
        add(new VehiclePassEvent(6579833L, "66974ae1a0a5d", VehicleBrand.VOLKSWAGEN, 1721191660000L));
        add(new VehiclePassEvent(6579834L, "66974ae1a0a5b", VehicleBrand.VOLKSWAGEN, 1721191660000L));
        add(new VehiclePassEvent(6579835L, "66974ae1a0a5z", VehicleBrand.BYD, 1721191660000L));
        add(new VehiclePassEvent(6579831L, "66974ae1a0a5b", VehicleBrand.VOLKSWAGEN, 1721291660000L));
        add(new VehiclePassEvent(6579831L, "66974ae1a0a5b", VehicleBrand.VOLKSWAGEN, 1721291660000L));
    }};

    private static CarCountDao carCountDao;
    private static KafkaProducer<String, VehiclePassEvent> producer;
    private static Thread consumerThread; 
        
    @BeforeAll()
    public static void setup() {
        carCountDao = new CarCountDao();
        carCountDao.flushAllCars();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        producer = new KafkaProducer<>(props);
        consumerThread = createConsumerThread();
    }

    @AfterAll()
    public static void tearDown() {
        carCountDao.flushAllCars();
        producer.close();

        try {
            consumerThread.join(2000);
        
            if (consumerThread.isAlive()) {
                consumerThread.interrupt();
            }
        } catch (Exception e) {
            System.err.println("Error during consumer thread shutdown: " + e.getMessage());
        }
    }

    @Test()
    @Timeout(value = 60)
    public void simulateCarProducer() throws InterruptedException {
        for (VehiclePassEvent event : CarList) {
            ProducerRecord<String, VehiclePassEvent> record = new ProducerRecord<>(TOPIC,  event.getVehicleId(), event);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("Error sending message: " + exception.getMessage());
                }
            });
        }

        producer.flush();
        
        Thread consumerThread = createConsumerThread();   
        consumerThread.start();

        await().atMost(60, TimeUnit.SECONDS)
           .until(() -> 
                        carCountDao.getCarCountForBrandAndDate("VOLKSWAGEN", "17-07-2024") == 2 &&
                        carCountDao.getCarCountForBrandAndDate("BYD", "17-07-2024") == 2 &&
                        carCountDao.getCarCountForBrandAndDate("VOLKSWAGEN", "18-07-2024") == 1
                        );
    }

    private static Thread createConsumerThread() {
       return new Thread(() -> {
            CarCounterConsumer consumer = new CarCounterConsumer();

            try {
                consumer.startPolling();
            } finally {
                consumer.shutdown();
            }
        });
    }
}


