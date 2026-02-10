import { useState, useEffect } from "react";
import VideoPlayer from "./VideoPlayer";
import { useNavigate } from "react-router-dom";

export default function VideoHistory() {
  const[videos, setVideos] = useState([]);
  const navigate = useNavigate();   

  useEffect(() => {
    const history = JSON.parse(localStorage.getItem("videoHistory")) || [];
    setVideos(history);
  }, [])

  const handleDelete = (indexToDelete, e) => {
    e.stopPropagation();

    const updatedVideos = videos.filter(
      (_, index) => index !== indexToDelete 
    );

    setVideos(updatedVideos); 
    localStorage.setItem("videoHistory", JSON.stringify(updatedVideos))
  };

  const handleOpenVideo = (fileName) => {
    navigate(`/watch/${fileName}`);
  }

  if(videos.length === 0){
    return (
      <p className="text-gray-500">
        No uploaded videos yet.
      </p>
    )
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      {videos.map((video, index) => (
        <div 
          key={index} 
          onClick={() => handleOpenVideo(video.fileName)}
          className="border rounded p-4 shadow-sm"
        >
          <button
            onClick={() => handleDelete(index)}
            className="relative top-2 right-2 text-red-500 hover:text-red-700 text-sm"
          >
            Delete
          </button>

          <p className="font-medium mb-1">
            {video.originalName || video.fileName}
          </p>
          
          <p className="text-xs text-gray-500 mb-2">
            {new Date(video.uploadedAt).toLocaleString()}
          </p>
        </div>
      ))}
    </div>
  )
}
