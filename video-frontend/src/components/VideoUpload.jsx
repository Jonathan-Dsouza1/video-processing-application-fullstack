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
      return;
    }
    
    const file = selectedFile;
    const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
    const fileId = crypto.randomUUID();

    setStatus("Uploading...");
    setProgress(0);

    let finalFileName = null;

    for(let i = 0; i < totalChunks; i++){
      const start = i * CHUNK_SIZE;
      const end = Math.min(start + CHUNK_SIZE, file.size);
      const chunk = file.slice(start, end);

      const res = await uploadChunk(chunk, i, totalChunks, fileId);

      if(
        res?.data && 
        typeof res.data === "string" && 
        res.data.endsWith(".mp4")
      ) {
        finalFileName = res.data;
        setProcessedFileName(res.data);
        setStatus("Ready");
      }
      setProgress(Math.round(((i + 1) / totalChunks) * 100));
    }

    if(finalFileName){
      const history = JSON.parse(localStorage.getItem("videoHistory")) || [];

      history.unshift({
        originalName: selectedFile.name,
        fileName: finalFileName,
        uploadedAt: new Date().toISOString()
      })

      localStorage.setItem("videoHistory", JSON.stringify(history));
    }
  };
  
  return (
    <div className='space-y-4'>
      <div className='flex items-center gap-4'>
        <input 
          type="file" 
          accept="video/*" 
          onChange={handleFileSelect}
          className='border p-2 rounded'
        />

        <button 
          onClick={handleUpload} 
          disabled={!selectedFile || status === "Uploading..."}
          className='bg-blue-500 text-white px-4 py-2 rounded disabled:bg-gray-400'
        >
          Upload
        </button>
      </div>
      
      {status && (
        <div className='bg-gray-100 p-4 rounded'>
          <p className='font-medium'>Status: {status}</p>

          <div className='w-full bg-gray-300 rounded h-4 mt-2'>
            <div 
              className='bg-blue-500 h-4 rounded'
              style={{ width: `${progress}%` }}
            />
          </div>

          <p className='text-sm mt-1'>{progress}%</p>
        </div>
      )}

      {processedFileName && (
        <div className='mt-4'>
          <h3 className='font-semibold mb-2'>{processedFileName}</h3>
          <VideoPlayer fileName = {processedFileName} />
        </div>
      )}
    </div>
  );
}
