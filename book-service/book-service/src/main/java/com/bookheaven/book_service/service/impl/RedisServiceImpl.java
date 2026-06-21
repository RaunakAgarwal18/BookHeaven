package com.bookheaven.book_service.service.impl;

import com.bookheaven.book_service.service.RedisService;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class RedisServiceImpl implements RedisService {

    private StringRedisTemplate redisTemplate;

    public void saveKey(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void saveKeyWithTimeout(String key, String value, Integer time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.MINUTES);
    }

    public String getKey(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    public boolean containsKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public void incrementValueByOne(String key) {
        redisTemplate.opsForValue().increment(key);
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    @Override 
    public void deleteKeyWithPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
