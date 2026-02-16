package com.example.video_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoStatusService {
    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "video:status:";

    public void setProcessing(String videoId){
        redisTemplate.opsForValue().set(PREFIX + videoId, "PROCESSING");
    }

    public void setReady(String videoId){
        redisTemplate.opsForValue().set(PREFIX + videoId, "READY");
    }

    public String getStatus(String videoId){
        return redisTemplate.opsForValue().get(PREFIX + videoId);
    }

    public void delete(String videoId){
        redisTemplate.delete(PREFIX + videoId);
    }
}
