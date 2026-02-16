package com.example.video_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UploadService {
    private static final String TEMP_DIR = "uploads/tmp/";
    private static final String FINAL_DIR = "uploads/final/";

    private final VideoStatusService videoStatusService;

    public String saveChunk(MultipartFile chunk, int index, int total, String fileId) {
        try {
            Path dir = Paths.get(TEMP_DIR + fileId);
            Files.createDirectories(dir);

            Path chunkPath = dir.resolve(index + ".part");

            Files.copy(
                    chunk.getInputStream(),
                    chunkPath,
                    StandardCopyOption.REPLACE_EXISTING
            );
            // If last chunk -> merge
            long uploadedChunks;
            try (Stream<Path> files = Files.list(dir)){
                uploadedChunks = files.count();
            }
            if(uploadedChunks == total){
                mergeChunks(fileId, total);
                videoStatusService.setProcessing(fileId);
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
        Path tempDir = Paths.get(TEMP_DIR, fileId);
        Path finalFile = Paths.get(FINAL_DIR + fileId + ".mp4");

        Files.createDirectories(finalFile.getParent());

        try(OutputStream out = Files.newOutputStream(finalFile)) {
            for(int i = 0; i < total; i++){
                Path chunkPath = tempDir.resolve(i + ".part");
                Files.copy(chunkPath, out);
            }
        }

        for(int i = 0; i < total; i++){
            Files.deleteIfExists(tempDir.resolve(i + ".part"));
        }
        Files.deleteIfExists(tempDir);
    }
}
