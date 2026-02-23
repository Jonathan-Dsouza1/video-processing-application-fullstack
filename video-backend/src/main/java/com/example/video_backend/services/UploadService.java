package com.example.video_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UploadService {
    private static final String TEMP_DIR = "uploads/tmp/";
    private static final String FINAL_DIR = "uploads/final/";

    private final VideoStatusService videoStatusService;
    private final MinioService minioService;
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("mp4", "mov", "mkv", "avi", "webm");

    public boolean saveChunk(
            MultipartFile chunk,
            int index,
            int total,
            String fileId,
            String title
    ) {
        try {
            String extension = getExtension(title);

            if(index == 0){
                if(!ALLOWED_EXTENSIONS.contains(extension)){
                    throw new RuntimeException("Unsupported video format: " + extension);
                }
            }

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
            try (Stream<Path> files = Files.list(dir)) {
                uploadedChunks = files.count();
            }
            if(uploadedChunks == total){
                Path mergedFile = mergeChunks(fileId, total, extension);

                String objectName = fileId + "/original." + extension;
                String contentType = Files.probeContentType(mergedFile);
                if(contentType == null){
                    contentType = "video/" + extension;
                }

                minioService.uploadFile(mergedFile, objectName, contentType);

                System.out.println("Deleted: " + mergedFile);
                Files.deleteIfExists(mergedFile);

                videoStatusService.setProcessing(fileId);

                System.out.println("Uploaded to MinIO: " + objectName);
                return true;
            }
        } catch (IOException e){
            throw new RuntimeException("Chunk upload failed", e);
        }
        return false;
    }

    private Path mergeChunks(String fileId, int total, String extension) throws IOException {
        Path tempDir = Paths.get(TEMP_DIR, fileId);
        Path finalFile = Paths.get(FINAL_DIR + fileId + "." + extension);

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

        return finalFile;
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf(".");
        if (dot == -1) return "";
        return fileName.substring(dot + 1).toLowerCase();
    }
}
