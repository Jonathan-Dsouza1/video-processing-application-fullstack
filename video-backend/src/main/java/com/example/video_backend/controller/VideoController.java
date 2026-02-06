package com.example.video_backend.controller;

import com.example.video_backend.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = "http://localhost:5173")
public class VideoController {
    private final UploadService uploadService;

    public VideoController(UploadService uploadService){
        this.uploadService = uploadService;
    }

    @PostMapping("/chunk")
    public ResponseEntity<?> uploadChunk(
            @RequestParam MultipartFile chunk,
            @RequestParam int index,
            @RequestParam int total,
            @RequestParam String fileId
    ) {
        System.out.println("Received chunk " + index + " of " + fileId);
        String processedFileName = uploadService.saveChunk(chunk, index, total, fileId);

        if(processedFileName != null){
            return ResponseEntity.ok(processedFileName);
        }
        return ResponseEntity.ok("Chunk recieved");
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
}
