package com.anze.CarCounter.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.anze.CarCounter.Config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class CarCountDao {
    private static final String COUNT_KEY_PREFIX = "count:";
    private static final Set<String> ALL_BRANDS = Set.of(
        "VOLKSWAGEN",
        "BMW",
        "NISSAN",
        "TOYOTA",
        "FORD",
        "HONDA",
        "BYD",
        "TESLA",
        "HYUNDAI",
        "OTHER"
      );

    /**
    * We use Redis lua script to ensure atomicity when updating. 
    * If sadd returns 1 we have a unique vehicle ID + date combination, so INCR is called on the count.
    * The vehicle IDs are stored in dates as keys in a set and the count is stored in count:date:brand key.
    **/
    private static final String UPDATE_COUNTER_SCRIPT = 
        "if redis.call('SADD', KEYS[1], ARGV[1]) == 1 then\n" +
        "   return redis.call('INCR', KEYS[2])\n" +
        "else\n" +
        "   return nil\n" +
        "end";

    private JedisPool jedisPool;

    public CarCountDao() {
        if (jedisPool == null) {
            jedisPool = new JedisPool(Config.REDIS_HOST, Config.REDIS_PORT);
        }
    }

    public Boolean insertVehicle(String date, String vehicleId, String brand) {
        try (Jedis jedis = jedisPool.getResource()) {
            String countKey = COUNT_KEY_PREFIX + date + ":" + brand.toLowerCase();
            Object result = jedis.eval(UPDATE_COUNTER_SCRIPT, 2, date, countKey, vehicleId);
            
            return (result != null) ? true : false;
        }
    }

    public long getCarCountForBrandAndDate(String brand, String dateInString) {
        String key = COUNT_KEY_PREFIX + dateInString + ":" + brand.toLowerCase();

        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(key);
            return value == null ? 0 : Long.parseLong(value);
        }
    }

    public Map<String, Long> getAllBrandCountsForDate(String dateInString) {
        String keyMask = COUNT_KEY_PREFIX + dateInString + ":*";
        Map<String, Long> brandCounts = new HashMap<>();

        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> vehicleKeys = jedis.keys(keyMask);

            for (String brand : ALL_BRANDS) {
                String key = COUNT_KEY_PREFIX + dateInString + ":" + brand.toLowerCase();
                if (!vehicleKeys.contains(key)) {
                    brandCounts.put(brand, 0L);
                } else {
                    String value = jedis.get(key);
                    brandCounts.put(brand, value == null ? 0L : Long.parseLong(value));
                }
            }

            return brandCounts;
        }
    }

    public void flushAllCars() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushAll();
        }
    }
}
