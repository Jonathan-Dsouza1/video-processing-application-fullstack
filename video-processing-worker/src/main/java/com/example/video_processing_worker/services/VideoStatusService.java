package com.example.video_processing_worker.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoStatusService {
    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "video:status:";

    public void setRedisStatus(String videoId, RedisStatus status){
        redisTemplate.opsForValue().set(PREFIX + videoId, status.name());
    }

    public void delete(String videoId){
        redisTemplate.delete(PREFIX + videoId);
    }
}
