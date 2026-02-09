import { useState } from 'react';
import { uploadChunk } from '../api/videoApi';
import VideoPlayer from './VideoPlayer';

const CHUNK_SIZE = 10 * 1024 * 1024 // 10MB

export default function VideoUpload() {
  const [progress, setProgress] = useState(0);
  const [status, setStatus] = useState("");
  const [processedFileName, setProcessedFileName] = useState(null);
  const [selectedFile, setSelectedFile] = useState(null);

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setSelectedFile(file);
    setProgress(0);
    setStatus("");
    setProcessedFileName(null);
  }

  const handleUpload = async () => {
    if(!selectedFile){
      alert("Please select a video first.");  
    }
    
    const file = selectedFile;
    const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
    const fileId = crypto.randomUUID();

    setStatus("Uploading...");
    setProgress(0);

    for(let i = 0; i < totalChunks; i++){
      const start = i * CHUNK_SIZE;
      const end = Math.min(start + CHUNK_SIZE, file.size);
      const chunk = file.slice(start, end);

      const res = await uploadChunk(chunk, i, totalChunks, fileId);

      if(
        res?.data && typeof res.data === "string" && res.data.endsWith(".mp4")
      ) {
        setProcessedFileName(res.data);
        setStatus("Ready");
      }
      setProgress(Math.round(((i + 1) / totalChunks) * 100));
    }
  };
  
  return (
    <div>
      <input type="file" accept="video/*" onChange={handleFileSelect}/>
      <button 
        onClick={handleUpload} 
        disabled={!selectedFile || status === "Uploading..."}
      >
        Upload
      </button>
      
      {status && <p>Progress: {progress}%</p>}
      {status && <p>Status: {status}</p>}

      {processedFileName && (
        <VideoPlayer fileName = {processedFileName} />
      )}
    </div>
  );
}
