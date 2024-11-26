package com.cp.classpay.service.cache;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class BookingLockService {

    private final RedissonClient redissonClient;

    public BookingLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public boolean lockForBooking(Long userId, Long classId) {
        RLock lock = redissonClient.getLock("booking_lock:" + userId + ":" + classId);
        try {
            return lock.tryLock(60, TimeUnit.SECONDS); // Adjust lock timeout as necessary
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void releaseBookingLock(Long userId, Long classId) {
        RLock lock = redissonClient.getLock("booking_lock:" + userId + ":" + classId);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
