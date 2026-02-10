package com.example.video_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {

    @Id
    private String videoId;

    private String title;

    private String storageName;

    private String contentType;

    private LocalDateTime uploadedAt;

    private String filePath;
}
