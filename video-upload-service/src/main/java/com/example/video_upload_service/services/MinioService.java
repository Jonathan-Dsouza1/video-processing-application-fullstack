package com.example.video_upload_service.services;

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

    public void deleteFolder(String prefix){
        try {
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );

            for(Result<Item> result : objects){
                String objectName = result.get().objectName();

                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectName)
                                .build()
                );

                System.out.println("Deleted from MinIO: " + objectName);
            }
        } catch (Exception e){
            throw new RuntimeException("Failed to delete MinIO folder: " + prefix, e);
        }
    }
}
