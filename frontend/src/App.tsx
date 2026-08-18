import { Route, Routes } from "react-router-dom";
import { AppShell } from "./layout/AppShell";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { AuthProvider } from "./auth/AuthContext";
import { RecordFormPage, RecordPage, RecordTypePage } from "./pages/RecordPage";
import { CalendarPage } from "./pages/CalendarPage";
import { ProfilePage } from "./pages/ProfilePage";
import { CmsPage } from "./pages/CmsPage";

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/cms" element={<CmsPage />} />
        <Route
          path="/*"
          element={
            <AppShell>
              <Routes>
                <Route path="/" element={<RecordPage />} />
                <Route path="/record" element={<RecordTypePage />} />
                <Route path="/record/consume" element={<RecordFormPage type="CONSUME" />} />
                <Route path="/record/intake" element={<RecordFormPage type="INTAKE" />} />
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
