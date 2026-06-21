package com.bookheaven.email_service.component;

import com.bookheaven.email_service.service.RedisService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@AllArgsConstructor
public class ProcessedMessageTracker {

    private static final Integer timeout = 1440;
    private RedisService redisService;

    public boolean isAlreadyProcessed(String messageId) {
        if (messageId == null) return false;
        return redisService.containsKey(messageId);
    }
    public void markProcessed(String messageId) {
        if (messageId == null) return;
        redisService.saveKeyWithTimeout(messageId,"1", timeout);
        log.debug("Marked messageId [{}] as processed", messageId);
    }
}
