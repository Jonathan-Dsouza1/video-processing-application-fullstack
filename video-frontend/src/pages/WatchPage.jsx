import { useParams } from "react-router-dom";
import VideoPlayer from "../components/VideoPlayer";

export default function WatchPage() {
  const { videoId } =  useParams();
  return (
    <div className="p-6">
      <h2 className="text-xl font-semibold mb-4">Now Playing</h2>

      <VideoPlayer videoId={videoId}/>
    </div>
  )
}
