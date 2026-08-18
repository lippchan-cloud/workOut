import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./layout/AppShell";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { AuthProvider } from "./auth/AuthContext";
import { RecordFormPage, RecordPage, RecordTypePage } from "./pages/RecordPage";
import { CalendarPage } from "./pages/CalendarPage";
import { ProfileAccountPage, ProfileBodyPage, ProfilePage } from "./pages/ProfilePage";
import { RecordDetailPage } from "./pages/RecordDetailPage";
import { CmsPage } from "./pages/CmsPage";
import { ReportPage } from "./pages/ReportPage";

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/cms" element={<CmsPage />} />
        <Route path="/report/:id" element={<ReportPage />} />
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
                <Route path="/calendar/trends" element={<Navigate to="/profile/body" replace />} />
                <Route path="/calendar/records/:id" element={<RecordDetailPage />} />
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="/profile/body" element={<ProfileBodyPage />} />
                <Route path="/profile/account" element={<ProfileAccountPage />} />
              </Routes>
            </AppShell>
          }
        />
      </Routes>
    </AuthProvider>
  );
}
