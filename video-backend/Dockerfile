# Use lightweight Java image
FROM eclipse-temurin:17-jdk-jammy

# Install FFmpeg
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*

# Set working directory inside container
WORKDIR /app

# Copy jar into container
COPY target/video-backend-0.0.1-SNAPSHOT.jar app.jar

# Create upload folders
RUN mkdir -p /app/uploads/tmp \
    && mkdir -p /app/uploads/final

# Expose Spring Boot port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]