import { useEffect, useRef, useState } from "react";
import { getHlsUrl } from "../api/videoApi";
import Hls from "hls.js";

export default function VideoPlayer({ videoId }) {
  const videoRef = useRef(null);
  const hlsRef = useRef(null);

  const[levels, setLevels] = useState([]);
  const [currentQuality, setCurrentQuality] = useState("Auto");
  const [showMenu, setShowMenu] = useState(false);

  const url = getHlsUrl(videoId);

  useEffect(() => {
    const video = videoRef.current;
    if(!video) return;

    // Safari native HLS
    if(video.canPlayType("application/vnd.apple.mpegurl")){
      video.src = url;
      return;
    }

    if(Hls.isSupported()){
      const hls = new Hls();
      hlsRef.current = hls;

      hls.loadSource(url);
      hls.attachMedia(video);

      hls.on(Hls.Events.MANIFEST_PARSED, () => {
        const availableLevels = hls.levels.map((levels, index) => ({
          index,
          label: `${level.height}p`,
        }));

        setLevels(availableLevels);
      });

      return () => {
        hls.destroy();
      };
    }
  }, [url]);

  const changeQuality = () => {
    const hls = hlsRef.current;
    if(!hls) return;

    hls.currentLevel = levelIndex;
    setCurrentQuality(label);
    setShowMenu(false);
  }
  
  return (
    <div className="relative w-[720px]">
      <video key={videoRef} className="w-[720px] rounded" ref={videoRef} controls />

      <div className="absolute bottom-3 right-3">
        <button
          onClick={() => setShowMenu(!showMenu)}
          className="bg-black/70 text-white px-2 py-1 text-sm rounded hover:bg-black"
        >
          {currentQuality} ⚙
        </button>

        {showMenu && (
          <div className="absolute bottom-10 right-0 bg-black/80 text-white rounded-md shadow">
            <div
              onClick={() => changeQuality(-1, "Auto")}
              className="px-3 py-1 cursor-pointer hover:bg-gray-700"
            >
              Auto
            </div>
            {levels.map((level) => (
              <div
                key={level.index}
                onClick={() =>
                  changeQuality(level.index, level.label)
                }
                className="px-3 py-1 cursor-pointer hover:bg-gray-700"
              >
                {level.label}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
