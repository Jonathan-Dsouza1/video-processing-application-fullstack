package com.example.video_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

@Service
public class UploadService {
    private static final String TEMP_DIR = "uploads/tmp/";
    private static final String FINAL_DIR = "uploads/final/";

    private final VideoProcessingService videoProcessingService;

    public UploadService(VideoProcessingService videoProcessingService){
        this.videoProcessingService = videoProcessingService;
    }

    public String saveChunk(MultipartFile chunk, int index, int total, String fileId) {
        try {
            Path dir = Paths.get(TEMP_DIR + fileId);
            Files.createDirectories(dir);

            Path chunkPath = dir.resolve(index + ".part");
            Files.write(chunkPath, chunk.getBytes());

            // If last chunk -> merge
            if(index == total - 1){
                mergeChunks(fileId, total);
                videoProcessingService.processAsync(fileId);

                String processedFileName = fileId + ".mp4";
                System.out.println("Video processing completed for " + fileId);
                return processedFileName;
            }
        } catch (IOException e){
            throw new RuntimeException("Chunk upload failed", e);
        }
        return null;
    }

    private void mergeChunks(String fileId, int total) throws IOException {
        Path finalFile = Paths.get(FINAL_DIR + fileId + ".mp4");
        Files.createDirectories(finalFile.getParent());

        try(OutputStream out = Files.newOutputStream(finalFile)) {
            for(int i = 0; i < total; i++){
                Path chunk = Paths.get(TEMP_DIR + fileId + "/" + i + ".part");
                Files.copy(chunk, out);
            }
        }
    }
}
