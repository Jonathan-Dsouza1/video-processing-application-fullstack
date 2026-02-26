package com.example.video_processing_worker.messaging;

import com.example.video_processing_worker.config.RabbitConfig;
import com.example.video_processing_worker.entities.Video;
import com.example.video_processing_worker.repositories.VideoRepository;
import com.example.video_processing_worker.services.RedisStatus;
import com.example.video_processing_worker.services.VideoProcessingService;
import com.example.video_processing_worker.services.VideoStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VideoProcessingWorker {

    private final VideoProcessingService videoProcessingService;
    private final VideoRepository videoRepository;
    private final VideoStatusService videoStatusService;

    @RabbitListener(
            queues = RabbitConfig.QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void process(VideoProcessingTask task){
        String videoId = task.getVideoId();
        System.out.println("Processing: " + videoId);

        try{
            videoProcessingService.processAllResolutions(videoId);

            boolean valid = videoProcessingService.validateAllResolutions(videoId);

            Video video = videoRepository.findById(videoId).orElseThrow();

            if(!valid){
                System.err.println("Validation failed for " + videoId);

                videoStatusService.setRedisStatus(videoId, RedisStatus.FAILED);
                video.setStatus("FAILED");
                videoRepository.save(video);
                return;
            }

            // Success
            videoStatusService.setRedisStatus(videoId, RedisStatus.READY);
            video.setStatus("READY");
            videoRepository.save(video);

            System.out.println("Video READY: " + videoId);

        } catch (Exception e) {
            System.out.println("Processing FAILED for: " + videoId);

            videoStatusService.setRedisStatus(videoId, RedisStatus.FAILED);

            Video video = videoRepository.findById(videoId).orElse(null);
            if(video != null){
                video.setStatus("FAILED");
                videoRepository.save(video);
            }
        }
    }
}
