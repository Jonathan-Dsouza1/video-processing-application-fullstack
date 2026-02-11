package com.example.video_backend.controller;

import com.example.video_backend.entities.Video;
import com.example.video_backend.services.UploadService;
import com.example.video_backend.services.VideoService;
import lombok.RequiredArgsConstructor;
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
                    .title(title)
                    .storageName(processedFileName)
                    .contentType("video/mp4")
                    .filePath("uploads/final/" + processedFileName)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            videoService.save(video);

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

    @GetMapping
    public ResponseEntity<List<Video>> getAllVideos(){
        List<Video> videos = videoService.getAllByLatestFirst();

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
}
