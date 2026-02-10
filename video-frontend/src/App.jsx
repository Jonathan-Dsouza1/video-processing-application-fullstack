import {Routes, Route, Link} from "react-router-dom";
import UploadsPage from "./pages/UploadsPage.jsx";
import HomePage from "./pages/HomePage.jsx";
import WatchPage from "./pages/WatchPage.jsx";

function App() {
  return (
    <div>
      <nav className="bg-[#3f3f48] text-white p-4 flex gap-4">
        <Link to='/'>Home</Link>
        <Link to='/uploads'>Uploads</Link>
      </nav>

      <Routes>
        <Route path='/' element={<HomePage/>} />
        <Route path='/uploads' element={<UploadsPage/>} />
        <Route path='/watch/:fileName' element={<WatchPage />}/>
      </Routes>
    </div>
  )
}

export default App
