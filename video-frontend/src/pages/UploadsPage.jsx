import { useState } from "react";
import VideoUpload from "../components/VideoUpload";
import VideoHistory from "../components/VideoHistory";

export default function UploadsPage() {
  const [activeTab, setActiveTab] = useState("history");

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <div className="max-w-8xl mx-auto bg-white shadow rounded-lg p-6">
        <h1 className="text=2xl font-bold mb-6">Uploads</h1>

        <div className="flex border-b mb-6">
          <button
            className={`px-4 py-2 font-medium ${
              activeTab === "history"
                ? "border-b-2 border-blue-500 text-blue-600"
                : "text-gray-500"
            }`}
            onClick={() => setActiveTab("history")}
          >
            My Videos
          </button>

          <button
            className={`px-4 py-2 font-medium ${
              activeTab === "upload"
                ? "border-b-2 border-blue-500 text-blue-600"
                : "text-gray-500"
            }`}
            onClick={() => setActiveTab("upload")}
          >
            Upload Videos
          </button>
        </div>

        {activeTab === "upload" && <VideoUpload />}
        {activeTab === "history" && <VideoHistory />}
      </div> 
    </div>
  );
}
