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
        System.out.println("Processing: " + task.getResolution());

        videoProcessingService.processResolution(
                task.getVideoId(),
                task.getResolution()
        );
        System.out.println("Resolution done: " + task.getResolution());

        checkAndMarkReady(task.getVideoId());
    }

    private void checkAndMarkReady(String videoId){
        String[] resolutions = {"480p", "720p", "1080p"};

        for(String res : resolutions){
            String objectName = videoId + "_" + res + ".mp4";
            boolean exists = minioService.objectExists(objectName);

            System.out.println("Checking: " + objectName + " -> " + exists);

            if(!exists) {
                return;
            }
        }

        String currentStatus = videoStatusService.getStatus(videoId);
        if("READY".equals(currentStatus)){
            return;
        }

        videoStatusService.setReady(videoId);
        System.out.println("All resolutions found. Setting READY");

        Video video = videoRepository.findById(videoId)
                .orElseThrow();

        video.setStatus("READY");
        videoRepository.save(video);

        System.out.println("Video READY: " + videoId);
    }
}
