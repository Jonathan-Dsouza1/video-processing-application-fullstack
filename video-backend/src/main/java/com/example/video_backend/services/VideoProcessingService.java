package com.example.video_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class VideoProcessingService {
    private final MinioService minioService;

    public void processResolution(String fileId, String resolution) {

        Path inputFile = null;

        try{
            String objectName = fileId + "/original.mp4";
            inputFile = minioService.downloadFile(objectName, fileId);

            String scale;
            switch (resolution) {
                case "480p":
                    scale = "854:480";
                    break;
                case "720p":
                    scale = "1280:720";
                    break;
                case "1080p":
                    scale = "1920:1080";
                    break;
                default:
                    throw new IllegalArgumentException("Unknown resolution " + resolution);
            }

            Path outputDir = Paths.get("uploads/tmp", fileId, resolution);
            Files.createDirectories(outputDir);

            runFfmpeg(inputFile.toString(), outputDir, scale);

            System.out.println(resolution + " HLS created for " + fileId);

            boolean uploadSuccess = true;

            // Upload entire folder to MinIO
            try (Stream<Path> paths = Files.walk(outputDir)){
                for(Path path : paths.filter(Files::isRegularFile).toList()) {
                    try {
                        String relative = outputDir.relativize(path)
                                .toString()
                                .replace("\\", "/");

                        String objectKey = fileId + "/" + resolution + "/" + relative;

                        minioService.uploadFile(path, objectKey, getContentType(path));

                        System.out.println("Uploaded " + objectKey);
                    } catch (Exception e) {
                        uploadSuccess = false;
                        throw new RuntimeException(e);
                    }
                }
            }
            if(uploadSuccess){
                deleteDirectory(outputDir);
            } else {
                System.out.println("Upload failed, keeping local files for debugging");
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if(inputFile != null) Files.deleteIfExists(inputFile);
            } catch (Exception ignored) {}
        }
    }

    private void runFfmpeg(String input, Path outputDir, String scale) throws Exception {
        Files.createDirectories(outputDir);

        String playlist = outputDir.resolve("index.m3u8").toString();
        String segmentPattern = outputDir.resolve("seg_%03d.ts").toString();

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", input,
                "-vf", "scale=" + scale,
                "-c:v", "libx264",
                "-preset", "veryfast",
                "-crf", "23",
                "-c:a", "aac",
                "-b:a", "128k",
                "-hls_time", "4",
                "-hls_playlist_type", "vod",
                "-hls_segment_filename", segmentPattern,
                playlist
        );

        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();

        System.out.println("HLS finished for " + outputDir + " with code: " + exitCode);
    }

    private String getContentType(Path path){
        if(path.toString().endsWith(".m3u8"))
            return "application/vnd.apple.mpegurl";
        if(path.toString().endsWith(".ts"))
            return "video/mp2t";
        return "application/octet-stream";
    }

    private void deleteDirectory(Path dir){
        try {
            if(Files.exists(dir)){
                Files.walk(dir)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e){
                                e.printStackTrace();
                            }
                        });
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
