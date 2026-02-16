package com.example.video_backend.controller;

import com.example.video_backend.config.RabbitConfig;
import com.example.video_backend.entities.Video;
import com.example.video_backend.messaging.VideoProcessingTask;
import com.example.video_backend.services.UploadService;
import com.example.video_backend.services.VideoService;
import com.example.video_backend.services.VideoStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

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
        System.out.println("Received chunk " + index + " of " + fileId);
        String processedFileName = uploadService.saveChunk(chunk, index, total, fileId);

        if(processedFileName != null){
            Video video = Video.builder()
                    .videoId(fileId)
                    .title(title.replace(".mp4", ""))
                    .storageName(processedFileName)
                    .contentType("video/mp4")
                    .filePath("uploads/final/" + processedFileName)
                    .uploadedAt(LocalDateTime.now())
                    .status("PROCESSING")
                    .build();

            videoService.save(video);

            rabbitTemplate.convertAndSend(RabbitConfig.QUEUE,  new VideoProcessingTask(fileId, "480p"));
            rabbitTemplate.convertAndSend(RabbitConfig.QUEUE,  new VideoProcessingTask(fileId, "720p"));
            rabbitTemplate.convertAndSend(RabbitConfig.QUEUE,  new VideoProcessingTask(fileId, "1080p"));

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(processedFileName);
        }
        return ResponseEntity.ok("Chunk received");
    }

    @GetMapping("/video/{fileName}")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable String fileName
    ) throws IOException {
        Path videoPath = Paths.get("uploads/final/" + fileName);
        Resource resource = new UrlResource(videoPath.toUri());

        if(!resource.exists()){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/mp4"))
                .body(resource);
    }

    @GetMapping("/video/{fileId}/{resolution}")
    public ResponseEntity<Resource> streamByResolution(
            @PathVariable String fileId,
            @PathVariable String resolution
    ) throws IOException {
        String filePath = "uploads/final/" + fileId + "_" + resolution + ".mp4";

        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());

        if(!resource.exists()){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/mp4"))
                .body(resource);
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
        String status = videoStatusService.getStatus(videoId);

        if(status == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }
}
