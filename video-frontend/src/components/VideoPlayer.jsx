import { useState } from "react";
import { getVideoByResolution } from "../api/videoApi";

export default function VideoPlayer({ fileName }) {
  if(!fileName) return null;

  const videoId = fileName.replace(".mp4", "");

  const [resolution, setResolution] = useState("720p");
  const [showMenu, setShowMenu] = useState(false);

  const qualities = ["1080p", "720p", "480p"];
  
  const videoUrl = getVideoByResolution(videoId, resolution);
  
  return (
    <div className="relative w-[720px]">
      <video key={videoUrl} className="w-[720px] rounded" controls>
        <source src={videoUrl} type="video/mp4" />
      </video>

      <div className="absolute bottom-3 right-3">
        <button
          onClick={() => setShowMenu(!showMenu)}
          className="bg-black/70 text-white px-2 py-1 text-sm rounded hover:bg-black"
        >
          {resolution} ⚙
        </button>

        {showMenu && (
          <div className="absolute bottom-10 right-0 bg-black/80 text-white rounded-md shadow">
            {qualities.map((q) => (
              <div
                key={q}
                onClick={() => {
                  setResolution(q);
                  setShowMenu(false);
                }}
                className={`px-3 py-1 cursor-pointer hover:bg-gray-700 ${
                  resolution === q ? "font-bold" : ""
                }`}
              >
                {q}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
