package com.cp.classpay.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void setWithExpiration(String key, Object value, long duration, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, duration, timeUnit);
    }

    public void setWithoutExpiration(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public void expire(String key, long duration, TimeUnit timeUnit) {
        redisTemplate.expire(key, duration, timeUnit);
    }

    public Long incrementBy(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long incrementBy(String key, long incrementBy) {
        return redisTemplate.opsForValue().increment(key, incrementBy);
    }

    public void decrementBy(String key, long decrementBy) {
        redisTemplate.opsForValue().decrement(key, decrementBy);
    }

    // Remove all elements from a Redis List, Trims list, effectively clearing it
    public void clearList(String key) {
        redisTemplate.opsForList().trim(key, 1, 0);
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize object", e);
        }
    }

    private <T> T deserialize(String json, CollectionType collectionType) {
        try {
            return objectMapper.readValue(json, collectionType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not deserialize object", e);
        }
    }

    public <T> void setList(String key, List<T> list, long duration, TimeUnit timeUnit) {
        if (list.isEmpty()) {
            redisTemplate.delete(key); // Clear key if list is empty
            return;
        }

        String serializedList = serialize(list);
        redisTemplate.opsForValue().set(key, serializedList);
        expire(key, duration, timeUnit);
    }


    public <T> List<T> getList(String key, Class<T> clazz) {
        String json = (String) redisTemplate.opsForValue().get(key);

        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }

        return deserialize(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }




    // --- Hash Operations for Field-Level Access ---
    public <T> void setHash(String key, T object, long duration, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForHash().putAll(key, objectMapper.convertValue(object, Map.class));
            expire(key, duration, timeUnit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store hash in Redis", e);
        }
    }

    public <T> T getHash(String key, Class<T> clazz) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) {
            return null;
        }
        return objectMapper.convertValue(entries, clazz);
    }


    // FIFO Logic: Add item to the queue (LPUSH)
    public <T> void pushToQueue(String key, T item) {
        String serializedItem = serialize(item);
        redisTemplate.opsForList().leftPush(key, serializedItem); // Add to the left (queue front)
    }

    // FIFO Logic: Retrieve and remove the item from the queue (RPOP)
    public <T> T popFromQueue(String key, Class<T> clazz) {
        String json = (String) redisTemplate.opsForList().rightPop(key); // Remove from the right (queue rear)

        if (json == null) {
            return null; // No items in the queue
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stores a class's end date in Redis ZSet with epoch seconds as the score.
     *
     * @param classId the ID of the class
     * @param endDate the end date of the class
     */
    public void saveClassEndDate(Long classId, ZonedDateTime endDate) {
        ZSetOperations<String, Long> zSetOps = getZSetOperationsForLong();
        long score = endDate.toEpochSecond();
        zSetOps.add("classEndDates", classId, score);
    }

    /**
     * Retrieves IDs of classes ending exactly at the current time (to the second).
     *
     * @return set of class IDs ending at the current epoch second
     */
    public Set<Long> getClassesEndingNow() {
        long currentEpochSecond = Instant.now().getEpochSecond();
        return getClassesEndingBetween(currentEpochSecond, currentEpochSecond);
    }

    /**
     * Retrieves IDs of classes ending within a buffer window around the current time.
     *
     * @param bufferSeconds the time buffer in seconds
     * @return set of class IDs ending within the buffer window
     */
    public Set<Long> getClassesEndingAroundNow(int bufferSeconds) {
        long currentEpochSecond = Instant.now().getEpochSecond();
        return getClassesEndingBetween(currentEpochSecond - bufferSeconds, currentEpochSecond + bufferSeconds);
    }

    /**
     * Retrieves IDs of classes ending within a specified time range.
     *
     * @param start the start of the time range in epoch seconds
     * @param end   the end of the time range in epoch seconds
     * @return set of class IDs ending within the specified time range
     */
    public Set<Long> getClassesEndingBetween(long start, long end) {
        ZSetOperations<String, Long> zSetOps = getZSetOperationsForLong();
        return zSetOps.rangeByScore("classEndDates", start, end);
    }


    @SuppressWarnings("unchecked")
    private ZSetOperations<String, Long> getZSetOperationsForLong() {
        return (ZSetOperations<String, Long>) (ZSetOperations<?, ?>) redisTemplate.opsForZSet();
    }

}
