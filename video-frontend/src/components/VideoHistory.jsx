import { useState, useEffect } from "react";
import { getAllVideos, deleteVideo } from "../api/videoApi";
import { useNavigate } from "react-router-dom";
import VideoDisplay from "./VideoDisplay";

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
    navigate(`/watch/${video.videoId}`);
  }

  if(videos.length === 0){
    return <p className="text-gray-500">No uploaded videos yet.</p>;
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {videos.map((video, index) => (
        <div 
          key={index} 
          className="bg-white rounded-lg shadow hover:shadow-md transition overflow-hidden"
        >
          <div
            className="cursor-pointer"
            onClick={() => handleOpenVideo(video)}
          >
            <VideoDisplay videoId={video.videoId} />
          </div>

          <div className="p-3">
            <div className="flex justify-between items-start"> 
              <p className="font-medium text-lg line-clamp-2 pr-2">
                {video.title}
              </p>

              <button
                onClick={(e) => handleDelete(video.videoId, e)}
                className=" hover:border-white border-red-600 border-2 p-1 rounded hover:bg-red-500 hover:text-white text-red-600 transition"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="w-5 h-5"
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
          </div>
          <p className="text-xs text-gray-500 mb-2 p-2">
            {new Date(video.uploadedAt).toLocaleString()}
          </p>
        </div>
      ))}
    </div>
  )
}
