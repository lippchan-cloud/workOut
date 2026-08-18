import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./layout/AppShell";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { AuthProvider } from "./auth/AuthContext";
import { RecordPage } from "./pages/RecordPage";
import { CalendarPage } from "./pages/CalendarPage";
import { ProfilePage } from "./pages/ProfilePage";

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/*"
          element={
            <AppShell>
              <Routes>
                <Route path="/" element={<RecordPage />} />
                <Route path="/record" element={<Navigate to="/" replace />} />
                <Route path="/calendar" element={<CalendarPage />} />
                <Route path="/profile" element={<ProfilePage />} />
              </Routes>
            </AppShell>
          }
        />
      </Routes>
    </AuthProvider>
  );
}
