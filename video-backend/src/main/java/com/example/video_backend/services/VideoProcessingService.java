package com.example.video_backend.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class VideoProcessingService {
    public void processResolution(String fileId, String resolution) {
        String input = "uploads/final/" + fileId + ".mp4";
        String output;
        String scale;

        switch (resolution) {
            case "480p":
                output = "uploads/final/" + fileId + "_480.mp4";
                scale = "854:480";
                break;

            case "720p":
                output = "uploads/final/" + fileId + "_720.mp4";
                scale = "1280:720";
                break;

            case "1080p":
                output = "uploads/final/" + fileId + "_1080.mp4";
                scale = "1920:1080";
                break;

            default:
                throw new IllegalArgumentException("Unknown resolution " + resolution);
        }
        try {
            runFfmpeg(input, output, scale);
            System.out.println(resolution + " completed for " + fileId);
        } catch (Exception e){
            e.printStackTrace();
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
