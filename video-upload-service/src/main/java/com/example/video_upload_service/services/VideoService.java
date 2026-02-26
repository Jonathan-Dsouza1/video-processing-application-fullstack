package com.example.video_upload_service.services;

import com.example.video_upload_service.entities.Video;

import java.util.List;

public interface VideoService {

    Video save(Video video);

    Video get(String videoId);

    void delete(String videoId);

    List<Video> getReadyVideos();
}
