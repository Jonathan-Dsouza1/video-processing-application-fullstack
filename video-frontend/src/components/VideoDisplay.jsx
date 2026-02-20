export default function VideoDisplay({videoId}) {
  if(!videoId) return null;

  const thumbnailUrl = `http://localhost:9000/videos/${videoId}/master.m3u8`;

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
