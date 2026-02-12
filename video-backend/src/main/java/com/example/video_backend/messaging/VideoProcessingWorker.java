package com.example.video_backend.messaging;

import com.example.video_backend.config.RabbitConfig;
import com.example.video_backend.services.VideoProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VideoProcessingWorker {

    private final VideoProcessingService videoProcessingService;

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
    }
}
