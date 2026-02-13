package com.example.video_backend.services.impl;

import com.example.video_backend.entities.Video;
import com.example.video_backend.repositories.VideoRepository;
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
            Path originalPath = Paths.get(video.getFilePath());

            Files.deleteIfExists(originalPath);

            String fileName = video.getStorageName();
            String baseName = fileName.substring(0, fileName.lastIndexOf("."));

            Path directory = originalPath.getParent();

            String[] resolutions = { "480p", "720p", "1080p" };

            for(String res : resolutions){
                Path resPath = directory.resolve(baseName + "_" + res + ".mp4");
                Files.deleteIfExists(resPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete video files", e);
        }

        videoRepository.deleteById(videoId);
    }
}
