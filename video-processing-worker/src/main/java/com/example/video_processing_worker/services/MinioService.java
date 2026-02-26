package com.example.video_processing_worker.services;

import io.minio.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class MinioService {
    @Value("${minio.bucket}")
    private String bucket;

    private final MinioClient minioClient;

    public MinioService(MinioClient minioClient){
        this.minioClient = minioClient;
    }

    public void uploadFile(Path filePath, String objectName, String contentType){
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .filename(filePath.toString())
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public boolean objectExists(String objectName){
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName).
                            build()
            );
            return true;
        } catch (io.minio.errors.ErrorResponseException e){
            if("NoSuchKey".equals(e.errorResponse().code())){
                return false;
            }
            throw new RuntimeException("MinIO error while checking object: " + objectName, e);
        } catch (Exception e) {
            throw new RuntimeException("MinIO connection error", e);
        }
    }

    public Path downloadFile(String objectName, String videoId) {
        try {
            Path tempFile = Paths.get("uploads/tmp", videoId + "_original.mp4");
            Files.createDirectories(tempFile.getParent());

            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .filename(tempFile.toString())
                            .build()
            );
            return tempFile;
        } catch (Exception e){
            throw new RuntimeException("Failed to download from MinIO", e);
        }
    }
}
