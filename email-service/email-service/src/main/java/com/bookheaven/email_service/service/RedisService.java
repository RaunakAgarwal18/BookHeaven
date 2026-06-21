package com.bookheaven.email_service.service;

public interface RedisService {
    public void saveKey(String key, String value);
    public void saveKeyWithTimeout(String key, String value, Integer time);
    public String getKey(String key);
    public boolean containsKey(String key);
    public void incrementValueByOne(String key);
    public void deleteKey(String key);
}