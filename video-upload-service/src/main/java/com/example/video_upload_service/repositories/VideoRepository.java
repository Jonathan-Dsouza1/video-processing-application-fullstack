package com.example.video_upload_service.repositories;

import com.example.video_upload_service.entities.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    List<Video> findByStatusOrderByUploadedAtDesc(String status);
}
