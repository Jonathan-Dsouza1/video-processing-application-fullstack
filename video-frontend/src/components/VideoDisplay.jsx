export default function VideoDisplay({fileName}) {
  if(!fileName) return null;

  const thumbnailUrl = `http://localhost:8080/upload/video/${fileName}`;

  return (
      <div className='w-full aspect-video bg-black rounded overflow-hidden'>
        <video
          src={thumbnailUrl}
          className='w-full h-full object-cover'
          muted
          preload='metadata'
        />
      </div>
  );
}
