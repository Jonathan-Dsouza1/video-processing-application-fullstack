package com.example.video_backend.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class VideoProcessingService {
    @Async
    public void processAsync(String fileId) {
        String input = "uploads/final/" + fileId + ".mp4";

        try {
            runFfmpeg(input, "uploads/final/" + fileId + "_480p.mp4", "854:480");
            runFfmpeg(input, "uploads/final/" + fileId + "_720p.mp4", "1280:720");
            runFfmpeg(input, "uploads/final/" + fileId + "_1080p.mp4", "1920:1080");

            System.out.println("Video processing completed for " + fileId);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void runFfmpeg(String input, String output, String scale) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
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
