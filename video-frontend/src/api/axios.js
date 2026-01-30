import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
  timeout: 600000 // 10 minutes
});

export default api;