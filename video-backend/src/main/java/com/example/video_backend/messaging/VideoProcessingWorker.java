package com.example.video_backend.messaging;

import com.example.video_backend.config.RabbitConfig;
import com.example.video_backend.entities.Video;
import com.example.video_backend.repositories.VideoRepository;
import com.example.video_backend.services.VideoProcessingService;
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

        checkAndMarkReady(task.getVideoId());
    }

    private void checkAndMarkReady(String videoId){
        String baseDir = "uploads/final/";
        String[] resolutions = {"480p", "720p", "1080p"};

        boolean allExist = true;

        for(String res : resolutions){
            Path path = Paths.get(baseDir + videoId + "_" + res + ".mp4");
            if(!Files.exists(path)) {
                allExist = false;
                break;
            }
        }

        if (allExist) {
            Video video = videoRepository.findById(videoId)
                    .orElseThrow();

            if(!"READY".equals(video.getStatus())){
                video.setStatus("READY");
                videoRepository.save(video);
                System.out.println("Video READY: " + videoId);
            }
        }
    }
}
