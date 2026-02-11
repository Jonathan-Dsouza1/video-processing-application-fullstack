export default function VideoPlayer({ fileName }) {
  if(!fileName) return null;
  
  return (
    <div className="mt-2 mb-2">
      <video 
        width="720" 
        controls
        src={`http://localhost:8080/upload/video/${fileName}`}
      />
    </div>
  );
}
