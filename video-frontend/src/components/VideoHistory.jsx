import { useState, useEffect } from "react";
import { getAllVideos, deleteVideo } from "../api/videoApi";
import { useNavigate } from "react-router-dom";
import VideoPlayer from "./VideoPlayer";

export default function VideoHistory() {
  const[videos, setVideos] = useState([]);
  const navigate = useNavigate();   

  const fetchVideos = () => {
    getAllVideos()
      .then(res => setVideos(res.data))
      .catch(() => setVideos([]));
  };

  useEffect(() => {
    fetchVideos();
  }, [])

  const handleDelete = async (videoId, e) => {
    e.stopPropagation();
    await deleteVideo(videoId);
    fetchVideos();
  };

  const handleOpenVideo = (video) => {
    navigate(`/watch/${video.storageName}`);
  }

  if(videos.length === 0){
    return <p className="text-gray-500">No uploaded videos yet.</p>;
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      {videos.map((video, index) => (
        <div 
          key={index} 
          className="border rounded p-4 shadow-sm"
        >
          <button
            onClick={(e) => handleDelete(video.videoId, e)}
            className="relative top-2 right-2 text-red-500 hover:text-red-700 text-sm"
          >
            Delete
          </button>
          <div onClick={() => handleOpenVideo(video)}>
            <VideoPlayer fileName={video.storageName}/>
            <p className="font-medium mb-1">
              {video.title}
            </p>
          </div>
          
          <p className="text-xs text-gray-500 mb-2">
            {new Date(video.uploadedAt).toLocaleString()}
          </p>
        </div>
      ))}
    </div>
  )
}
