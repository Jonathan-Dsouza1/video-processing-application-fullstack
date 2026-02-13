import api from "./axios";

export function uploadChunk(chunk, index, total, fileId, selectedFile, onProgress) {
  const formData = new FormData();
  formData.append("chunk", chunk);
  formData.append("index", index);
  formData.append("total", total);
  formData.append("fileId", fileId);
  formData.append("title", selectedFile.name);
  
  return api.post("/upload/chunk", formData, {
    headers: { "Content-Type": "multipart/form-data" },
    onUploadProgress: (progressEvent) => {
      if(onProgress) {
        const percent = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        );
        onProgress(percent);
      }
    }
  });
};

export const getAllVideos  = () => {
  return api.get("/upload");
};

export const getVideoByResolution = (videoId, resolution) => {
  return api.get(`/upload/video/${videoId}/${resolution}`);
}

export const getVideoById = (videoId) => {
  return api.get(`/upload/${videoId}`);
}

export const deleteVideo = (videoId) => {
  return api.delete(`/upload/${videoId}`);
};
