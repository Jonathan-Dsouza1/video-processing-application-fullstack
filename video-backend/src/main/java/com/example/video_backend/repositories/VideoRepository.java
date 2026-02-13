package com.example.video_backend.repositories;

import com.example.video_backend.entities.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    List<Video> findByStatusOrderByUploadedAtDesc(String status);
}
