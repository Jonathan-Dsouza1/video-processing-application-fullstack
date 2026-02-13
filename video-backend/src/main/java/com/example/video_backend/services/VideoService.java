package com.example.video_backend.services;

import com.example.video_backend.entities.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {

    Video save(Video video);

    Video get(String videoId);

    void delete(String videoId);

    List<Video> getReadyVideos();
}
