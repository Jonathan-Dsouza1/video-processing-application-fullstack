import { useEffect, useRef, useState } from 'react';
import { getStatus, uploadChunk, getUploadedChunks, deleteVideo } from '../api/videoApi';
import VideoPlayer from './VideoPlayer';

const CHUNK_SIZE = 10 * 1024 * 1024 // 10MB

export default function VideoUpload() {
  const [queue, setQueue] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(-1);
  const [isUploading, setIsUploading] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [retryingIndex, setRetryingIndex] = useState(null);
  const [autoStart, setAutoStart] = useState(false);
  const pauseRef = useRef(false);

  useEffect(() => {
    if (!autoStart) return;
    if (isUploading) return;

    const hasPending = queue.some(
      item =>
        item.status === "STAGED" ||
        item.status === "Paused" ||
        item.status === "Paused (Network Issue)"
    );

    if (hasPending) {
      handleUpload();
      setAutoStart(false);
    }
  }, [queue, autoStart]);

  const handleFileSelect = (e) => {
    const files = Array.from(e.target.files);
    if (!files) return;

    const newItems = files.map(file =>({
      id: crypto.randomUUID(),
      file,
      progress: 0,
      status: "STAGED",
      processedFileName: null
    }));

    setQueue(prev => [...prev, ...newItems]);
  }

  const handleResume = () => {
    if (queue.length === 0) return;

    pauseRef.current = false;
    setIsPaused(false);
    handleUpload();
  };

  const updateQueue = (index, changes) => {
    setQueue(prev =>
      prev.map((item, i) => 
        i == index ? { ...item, ...changes} : item
      )
    );
  };

  const uploadSingleFile = async (item, index) => {
    const file = item.file;

    const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
    let fileId = item.id;

    const res = await getUploadedChunks(fileId);
    const uploaded = new Set(res.data);

    updateQueue(index, { status: "Uploading", progress: 0 });

    for(let i = 0; i < totalChunks; i++){
      if(uploaded.has(i)) continue;

      if(pauseRef.current){
        updateQueue(index, { status: "Paused" });
        return false;
      }

      const start = i * CHUNK_SIZE;
      const end = Math.min(start + CHUNK_SIZE, file.size);
      const chunk = file.slice(start, end);

      try {
        const res = await uploadChunk(
          chunk,
          i,
          totalChunks,
          fileId,
          file,
          (chunkPercent) => {
            const overallPercent = Math.round(
              ((i + chunkPercent / 100) / totalChunks) * 100
            );
            updateQueue(index, { progress: overallPercent });
          }
        );

      } catch {
        pauseRef.current = true;
        setIsPaused(true);
        setIsUploading(false);
        
        updateQueue(index, { status: "Paused (Network Issue)" });
        return false;
      }
    }
    
    updateQueue(index, { status: "Processing" });
    await pollUntilReady(fileId, index);

    return true;
  }

  const startUploadClicked = () => {
    setAutoStart(true);
  }

  const handleUpload = async () => {
    if(queue.length === 0){
      alert("Please select videos first.");
      return;
    }

    if(isUploading) return;

    pauseRef.current = false;
    setIsPaused(false);
    setIsUploading(true);

    for(let i = 0; i < queue.length; i++){
      if(pauseRef.current) break;

      const item = queue[i];

      if (
        item.status !== "STAGED" &&
        item.status !== "Paused" &&
        item.status !== "Paused (Network Issue)"
      ) continue;

      const success = await uploadSingleFile(item, i);

      if(!success) break;
    }

    setIsUploading(false);
  };

  const pollUntilReady = (videoId, index) => {
    return new Promise((resolve) => {
      let completed = false;
      const interval = setInterval(async () =>{
        if(completed) return;
        try {
          const res = await getStatus(videoId);
  
          const status = res.data.trim();
  
          if(status === "READY"){
            completed = true;
            updateQueue(index, {
              status: "Ready",
              videoId: videoId
            });
            clearInterval(interval);
            resolve();
          }
          else if(status === "FAILED"){
            completed = true;
            updateQueue(index, {
              status: "Failed",
              videoId: videoId,
              error: "Processing Failed"
            });
            clearInterval(interval);
            resolve();
          }
        } catch (err){
          console.error("Polling error", err);
          console.error("Status: ", err.response?.status);
          console.error("Data: ", err.response?.data);
  
          if (err.response && err.response.status === 403) {
            completed = true;
            clearInterval(interval);
          }
  
          if (err.response && err.response.status === 404){
            console.log("Status not found yet.");
          }
        }
      }, 3000);
    });
  };

  const handleReupload = async (index) => {
    const item = queue[index];

    try{
      setRetryingIndex(index);

      if(item.videoId){
        await deleteVideo(item.videoId);
      }

      const newId = crypto.randomUUID();
      
      setQueue(prev => 
        prev.map((q, i) => 
          i === index
            ? {
                ...q,
                id: newId,
                progress: 0,
                status: "STAGED",
                videoId: null,
                error: null
              }
            : q
        )      
      );

      setAutoStart(true);

    } catch (err) {
      console.error("Re-upload failed: ", err);
      alert("Failed to delete previous video");
    } finally {
      setRetryingIndex(null);
    }
  };
  
  return (
    <div className='space-y-4'>
      <div className='flex items-center gap-3'>
        <input 
          type="file" 
          accept="video/*"
          multiple
          onChange={handleFileSelect}
          className='border p-2 rounded'
        />

        <button 
          onClick={startUploadClicked} 
          disabled={!queue || queue.length === 0 || isUploading}
          className='bg-blue-500 text-white px-4 py-2 rounded disabled:bg-gray-400'
        >
          Start Upload
        </button>

        {isUploading && (
          <button
            onClick={() => {
              pauseRef.current = true;
              setIsPaused(true);
              setIsUploading(false);
            }}
            className="bg-yellow-500 text-white px-4 py-2 rounded"
          >
            Pause
          </button>
        )}

        {!isUploading && isPaused && (
          <button
            onClick={handleResume}
            className="bg-green-500 text-white px-4 py-2 rounded"
          >
            Resume
          </button>
        )}
      </div>

      <div className='space-y-3'>
        {queue.map((item, index) => (
          <div
            key={item.id}
            className={`p-4 rounded border
              ${index === currentIndex ? "border-blue-500 bg-blue-50" : "border-gray-200 bg-gray-100"}`}
          >
            <div className='flex justify-between items-center'>
              <div>
                <p className='font-medium'>{item.file.name}</p>
                <p className=' text-sm text-gray-600'>
                  Status: {item.status}
                </p>
                {item.status === "Failed" && (
                  <button
                    onClick={() => handleReupload(index)}
                    disabled={retryingIndex === index}
                    className={`mt-2 px-3 py-1 text-white rounded text-sm ${
                      retryingIndex === index
                        ? "bg-gray-400 cursor-not-allowed"
                        : "bg-red-500 hover:bg-red-600"
                    }`}
                  >
                    {retryingIndex === index ? "Retrying..." : "Re-upload"}
                  </button>
                )}
              </div>

              {item.status === "STAGED" && (
                <button
                onClick={() => 
                  setQueue(prev => prev.filter((_, i) => i !== index))
                }
                className='text-red-500 text-sm'
                >
                  Remove
                </button>
              )}
            </div>

            <div className='w-full bg-gray-300 rounded h-3 mt-3'>
              <div
                className={`h-3 rounded ${
                  item.status === "Ready"
                  ? "bg-green-500"
                  : item.status === "Paused"
                  ? "bg-yellow-500"
                  : "bg-blue-500"
                }`}
                style={{ width: `${item.progress}%` }}
              />
            </div>

            <p className='text-xs mt-1'>{item.progress}%</p>
            {item.videoId &&
            item.status !== "Failed"  && (
              <div className='mt-3'>
                <VideoPlayer videoId={item.videoId} />
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
