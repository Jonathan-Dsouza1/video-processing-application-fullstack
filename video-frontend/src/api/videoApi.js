import api from "./axios";

export function uploadChunk(chunk, index, total, fileId, onProgress) {
  const formData = new FormData();
  formData.append("chunk", chunk);
  formData.append("index", index);
  formData.append("total", total);
  formData.append("fileId", fileId);
  
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
