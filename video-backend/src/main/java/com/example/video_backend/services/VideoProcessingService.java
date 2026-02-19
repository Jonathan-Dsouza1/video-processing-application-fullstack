package com.example.video_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class VideoProcessingService {
    private final MinioService minioService;

    public void processResolution(String fileId, String resolution) {

        Path inputFile = null;
        Path outputFile = null;

        try{
            String objectName = fileId + "/original.mp4";
            inputFile = minioService.downloadFile(objectName, fileId);

            String scale;
            String outputName;
            switch (resolution) {
                case "480p":
                    scale = "854:480";
                    outputName = fileId + "_480p.mp4";
                    break;

                case "720p":
                    scale = "1280:720";
                    outputName = fileId + "_720p.mp4";
                    break;

                case "1080p":
                    scale = "1920:1080";
                    outputName = fileId + "_1080p.mp4";
                    break;

                default:
                    throw new IllegalArgumentException("Unknown resolution " + resolution);
            }

            outputFile = Paths.get("uploads/tmp", outputName);
            Files.createDirectories(outputFile.getParent());

            runFfmpeg(inputFile.toString(), outputFile.toString(), scale);

            System.out.println(resolution + " completed for " + fileId);

            String outputObject = fileId + "/" + resolution + ".mp4";
            minioService.uploadFile(outputFile, outputObject, "video/mp4");
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if(inputFile != null) Files.deleteIfExists(inputFile);
                if(outputFile != null) Files.deleteIfExists(outputFile);
            } catch (Exception ignored) {}
        }
    }

    private void runFfmpeg(String input, String output, String scale) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-threads", "2",
                "-i", input,
                "-vf", "scale=" + scale,
                "-c:v", "libx264",
                "-preset", "veryfast",
                "-crf", "23",
                "-c:a", "copy",
                output
        );

        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();

        System.out.println("FFmpeg finished for " + output + " with code: " + exitCode);
    }
}
