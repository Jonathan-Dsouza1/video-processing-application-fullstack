package com.example.video_backend.messaging;

import com.example.video_backend.config.RabbitConfig;
import com.example.video_backend.entities.Video;
import com.example.video_backend.repositories.VideoRepository;
import com.example.video_backend.services.MinioService;
import com.example.video_backend.services.VideoProcessingService;
import com.example.video_backend.services.VideoStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class VideoProcessingWorker {

    private final VideoProcessingService videoProcessingService;
    private final VideoRepository videoRepository;
    private final VideoStatusService videoStatusService;
    private final MinioService minioService;

    @RabbitListener(
            queues = RabbitConfig.QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void process(VideoProcessingTask task){
        String videoId = task.getVideoId();
        System.out.println("Processing: " + videoId);

        videoProcessingService.processAllResolutions(videoId);
        videoStatusService.setReady(videoId);

        Video video = videoRepository.findById(videoId).orElseThrow();
        video.setStatus("READY");
        videoRepository.save(video);

        System.out.println("Video READY: " + videoId);
    }
}
