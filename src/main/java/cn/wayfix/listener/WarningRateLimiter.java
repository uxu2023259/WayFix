package cn.wayfix.listener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

final class WarningRateLimiter {

    private final long intervalMillis;
    private final ConcurrentMap<String, Long> lastLoggedAt = new ConcurrentHashMap<>();

    WarningRateLimiter(long intervalMillis) {
        if (intervalMillis < 0) {
            throw new IllegalArgumentException("限频间隔不能为负数");
        }
        this.intervalMillis = intervalMillis;
    }

    boolean shouldLog(String key) {
        long now = System.currentTimeMillis();
        AtomicBoolean shouldLog = new AtomicBoolean(false);
        lastLoggedAt.compute(key, (ignored, previous) -> {
            if (previous == null || now - previous >= intervalMillis) {
                shouldLog.set(true);
                return now;
            }
            return previous;
        });
        return shouldLog.get();
    }
}
