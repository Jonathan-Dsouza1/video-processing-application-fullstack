package com.example.video_backend.services.impl;

import com.example.video_backend.entities.Video;
import com.example.video_backend.repositories.VideoRepository;
import com.example.video_backend.services.MinioService;
import com.example.video_backend.services.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final MinioService minioService;

    @Override
    public Video save(Video video) {
        return videoRepository.save(video);
    }

    @Override
    public Video get(String videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + videoId));
    }

    @Override
    public List<Video> getReadyVideos() {
        return videoRepository.findByStatusOrderByUploadedAtDesc("READY");
    }

    @Override
    public void delete(String videoId) {
        if(!videoRepository.existsById(videoId)){
            throw new RuntimeException("Video not found with id: " + videoId);
        }

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found " + videoId));

        try {
            String prefix = videoId + "/";

            minioService.deleteFolder(prefix);

            System.out.println("Delete video from MinIO " + videoId);
        } catch (   Exception e) {
            throw new RuntimeException("Failed to delete video from MinIO", e);
        }

        videoRepository.deleteById(videoId);
    }
}
