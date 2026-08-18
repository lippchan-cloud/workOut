import { Navigate, Route, Routes } from "react-router-dom";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<div>workOut</div>} />
      <Route path="/record" element={<Navigate to="/" replace />} />
      <Route path="/calendar" element={<div>calendar</div>} />
      <Route path="/profile" element={<div>profile</div>} />
      <Route path="/login" element={<div>login</div>} />
      <Route path="/register" element={<div>register</div>} />
    </Routes>
  );
}
