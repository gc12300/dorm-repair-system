package com.dormrepair.ai;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {

    public static final int DAILY_LIMIT = 10;
    private final Map<Long, Integer> counter = new ConcurrentHashMap<>();

    public boolean tryAcquire(Long userId) {
        int count = counter.getOrDefault(userId, 0);
        if (count >= DAILY_LIMIT) {
            return false;
        }
        counter.put(userId, count + 1);
        return true;
    }

    public int getRemaining(Long userId) {
        return Math.max(0, DAILY_LIMIT - counter.getOrDefault(userId, 0));
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void reset() {
        counter.clear();
    }
}
