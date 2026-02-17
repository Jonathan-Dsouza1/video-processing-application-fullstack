import { useRef, useState } from 'react';
import { getStatus, uploadChunk, getUploadedChunks } from '../api/videoApi';
import VideoPlayer from './VideoPlayer';

const CHUNK_SIZE = 10 * 1024 * 1024 // 10MB

export default function VideoUpload() {
  const [progress, setProgress] = useState(0);
  const [status, setStatus] = useState("");
  const [processedFileName, setProcessedFileName] = useState(null);
  const [selectedFile, setSelectedFile] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const pauseRef = useRef(false);

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setSelectedFile(file);
    setProgress(0);
    setStatus("");
    setProcessedFileName(null);
  }

  const handleResume = () => {
    if (!selectedFile) return;

    pauseRef.current = false;
    setIsPaused(false);
    setStatus("Resuming...");
    handleUpload();
  };


  const handleUpload = async () => {
    if(!selectedFile){
      alert("Please select a video first.");  
      return;
    }
    pauseRef.current = false;
    setIsPaused(false);
    setIsUploading(true);
    
    const file = selectedFile;
    const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
    let fileId = localStorage.getItem("uploadFileId");

    if(!fileId){
      fileId = crypto.randomUUID();
      localStorage.setItem("uploadFileId", fileId);
      localStorage.removeItem("uploadItemId");
      localStorage.removeItem("favorites");
    }

    const res = await getUploadedChunks(fileId);
    const uploaded = new Set(res.data);

    setStatus("Uploading...");
    setProgress(0);

    let finalFileName = null;

    for(let i = 0; i < totalChunks; i++){
      if(uploaded.has(i)){
        continue;
      }

      // Pause check
      if (pauseRef.current) {
        console.log("Upload paused");
        setStatus("Paused");
        setIsUploading(false);
        return;
      }

      const start = i * CHUNK_SIZE;
      const end = Math.min(start + CHUNK_SIZE, file.size);
      const chunk = file.slice(start, end);   

      try{
        const res = await uploadChunk(
          chunk, 
          i, 
          totalChunks, 
          fileId, 
          selectedFile, 
          (chunkPercent) => {
            const overallPercent = Math.round(((i + chunkPercent / 100) / totalChunks) * 100);
            setProgress(overallPercent);
          }
        );
  
        if(
          res?.data && 
          typeof res.data === "string" && 
          res.data.endsWith(".mp4")
        ) {
          finalFileName = res.data;
        }
        setProgress(Math.round(((i + 1) / totalChunks) * 100));
      } catch (err){
        console.log("Network error - auto paused");
        pauseRef.current = true;
        setIsPaused(true);
        setIsUploading(false);
        setStatus("Paused (network issue)");
        return;
      }
    }   

    if(finalFileName){
      setStatus("Processing...");
      pollUntilReady(fileId);
      setIsUploading(false);
      setIsPaused(false);
      localStorage.removeItem("uploadFileId");
    }
  };

  const pollUntilReady = (videoId) => {
    const interval = setInterval(async () =>{
      try {
        const res = await getStatus(videoId);

        const status = res.data.trim();

        if(status === "READY"){
          console.log("READY detected")
          setProcessedFileName(videoId + ".mp4");
          setStatus("Ready");
          clearInterval(interval);
        }
      } catch (err){
        console.error("Polling error", err);
        console.error("Status: ", err.response?.status);
        console.error("Data: ", err.response?.data);

        if (err.response && err.response.status === 403) {
          setStatus("Access denied (403)");
          clearInterval(interval);
        }

        if (err.response && err.response.status === 404){
          console.log("Status not found yet.");
        }
      }
    }, 3000);
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
        
        {isUploading ? (
          <button
            onClick={() => {
              pauseRef.current = true;
              setIsPaused(true);
              setIsUploading(false);
            }}
            disabled={!isUploading}
            className="bg-yellow-500 text-white px-4 py-2 rounded"
          >
            ⏸
          </button>
        ) : isPaused ? (
          <button
            onClick={handleResume}
            className="bg-green-500 text-white px-4 py-2 rounded"
          >
            ▶
          </button>
        ) : null}
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
          <h3 className='font-semibold mb-2'>{selectedFile?.name}</h3>
          <VideoPlayer fileName = {processedFileName} />
        </div>
      )}
    </div>
  );
}
