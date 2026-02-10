export default function VideoPlayer({ fileName }) {
  if(!fileName) return null;
  
  return (
    <div style={{marginTop: "2rem"}}>
      <video 
        width="720" 
        controls
        src={`http://localhost:8080/upload/video/${fileName}`}
      />
    </div>
  );
}
