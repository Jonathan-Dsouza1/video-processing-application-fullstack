package com.example.video_backend.controller;

import com.example.video_backend.config.RabbitConfig;
import com.example.video_backend.entities.Video;
import com.example.video_backend.messaging.VideoProcessingTask;
import com.example.video_backend.services.RedisStatus;
import com.example.video_backend.services.UploadService;
import com.example.video_backend.services.VideoService;
import com.example.video_backend.services.VideoStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class VideoController {
    private final UploadService uploadService;
    private final VideoService videoService;
    private final RabbitTemplate rabbitTemplate;
    private final VideoStatusService videoStatusService;

    @PostMapping("/chunk")
    public ResponseEntity<?> uploadChunk(
            @RequestParam MultipartFile chunk,
            @RequestParam int index,
            @RequestParam int total,
            @RequestParam String fileId,
            @RequestParam String title
    ) {
        System.out.println("Received chunk " + index + " of " + title);

        boolean isLastChunk = uploadService.saveChunk(chunk, index, total, fileId, title);

        if(isLastChunk){
            Video video = Video.builder()
                    .videoId(fileId)
                    .title(title.substring(0, title.indexOf(".")))
                    .uploadedAt(LocalDateTime.now())
                    .status("PROCESSING")
                    .build();

            videoService.save(video);

            rabbitTemplate.convertAndSend(
                    RabbitConfig.QUEUE,
                    new VideoProcessingTask(fileId)
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Upload complete, processing started");
        }
        return ResponseEntity.ok("Chunk received");
    }

    @GetMapping("/chunks/{fileId}")
    public List<Integer> getUploadedChunks(@PathVariable String fileId) throws IOException {
        Path dir = Paths.get("uploads/tmp", fileId);

        if(!Files.exists(dir)){
            return List.of();
        }

        try(Stream<Path> files = Files.list(dir)){
            return files
                    .map(p -> p.getFileName().toString().replace(".part", ""))
                    .map(Integer::parseInt)
                    .toList();
        }
    }

    @GetMapping
    public ResponseEntity<List<Video>> getAllVideos(){
        List<Video> videos = videoService.getReadyVideos();

        if(videos.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(videos);
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<Void> deleteVideo(@PathVariable String videoId){
        videoService.delete(videoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{videoId}")
    public ResponseEntity<String> getStatus(@PathVariable String videoId){
        RedisStatus status = videoStatusService.getRedisStatus(videoId);

        if(status == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status.name());
    }
}
