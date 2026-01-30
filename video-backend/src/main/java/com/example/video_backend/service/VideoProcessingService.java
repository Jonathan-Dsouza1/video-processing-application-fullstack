package com.example.video_backend.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class VideoProcessingService {
    @Async
    public void processAsync(String fileId) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", "uploads/final/" + fileId + ".mp4",
                    "-vf", "scale=1280:720",
                    "uploads/final/" + fileId + "_720p.mp4"
            );

            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();

            System.out.println("Video processing completed for " + fileId);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
