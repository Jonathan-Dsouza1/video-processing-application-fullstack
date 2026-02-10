package com.example.video_backend.services.impl;

import com.example.video_backend.entities.Video;
import com.example.video_backend.repositories.VideoRepository;
import com.example.video_backend.services.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
    public List<Video> getAllByLatestFirst() {
        return videoRepository.findAll(
                Sort.by(Sort.Direction.DESC, "uploadedAt")
        );
    }

    @Override
    public void delete(String videoId) {
        if(!videoRepository.existsById(videoId)){
            throw new RuntimeException("Video not found with id: " + videoId);
        }
        videoRepository.deleteById(videoId);
    }
}
