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
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
      {videos.map((video, index) => (
        <div 
          key={index} 
          className="border rounded p-4 shadow-sm"
        >
          <div className="flex justify-between"> 
            <p className="text-lg mb-1">
              {video.title}
            </p>

            <button
              onClick={(e) => handleDelete(video.videoId, e)}
              className="border rounded shadow-md p-2 text-red-500 hover:text-red-700 text-sm"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="w-4 h-4 text-red-600 border-red-600 group-hover:text-white"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={2}
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M6 7h12M9 7V5h6v2m-7 0v12a2 2 0 002 2h4a2 2 0 002-2V7"
                />
              </svg>
            </button>
          </div>

          <div onClick={() => handleOpenVideo(video)}>
            <VideoPlayer fileName={video.storageName}/>  
          </div>
          <p className="text-xs text-gray-500 mb-2">
            {new Date(video.uploadedAt).toLocaleString()}
          </p>
        </div>
      ))}
    </div>
  )
}
