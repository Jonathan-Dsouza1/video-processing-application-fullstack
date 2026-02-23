package com.example.video_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class VideoProcessingService {
    private final MinioService minioService;

    public void processAllResolutions(String videoId) {

        Path inputFile = null;

        try{
            inputFile = minioService.downloadFile(
                    videoId + "/original.mp4",
                    videoId
            );

            processHls(videoId, inputFile, "480p", "854:480");
            processHls(videoId, inputFile, "720p", "1280:720");
            processHls(videoId, inputFile, "1080p", "1920:1080");

            createMasterPlaylist(videoId);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if(inputFile != null) Files.deleteIfExists(inputFile);
                Path TmpDir = Paths.get("uploads/tmp", videoId);
                deleteDirectory(TmpDir);
            } catch (Exception ignored) {}
        }
    }

    private void processHls(String videoId, Path inputFile, String resolution, String scale) throws Exception {
        Path outputDir = Paths.get("uploads/tmp", videoId, resolution);
        Files.createDirectories(outputDir);

        runFfmpeg(inputFile.toString(), outputDir, scale);

        Files.walk(outputDir)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        String relative = outputDir.relativize(path)
                                .toString()
                                .replace("\\", "/");

                        String objectKey = videoId + "/" + resolution + "/" + relative;

                        minioService.uploadFile(path, objectKey, getContentType(path));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        deleteDirectory(outputDir.getParent());
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

    private void createMasterPlaylist(String videoId) throws Exception  {
        Path master = Paths.get("uploads/tmp", videoId, "master.m3u8");
        Files.createDirectories(master.getParent());

        String content =
                    "#EXTM3U\n" +
                    "#EXT-X-STREAM-INF:BANDWIDTH=2800000,RESOLUTION=1920x1080\n" +
                    "1080p/index.m3u8\n" +
                    "#EXT-X-STREAM-INF:BANDWIDTH=1400000,RESOLUTION=1280x720\n" +
                    "720p/index.m3u8\n" +
                    "#EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=854x480\n" +
                    "480p/index.m3u8\n";

        Files.write(master, content.getBytes());

        minioService.uploadFile(
                master,
                videoId + "/master.m3u8",
                "application/x-mpegurl"
        );

        Files.deleteIfExists(master);

        System.out.println("Master playlist created for " + videoId);
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
