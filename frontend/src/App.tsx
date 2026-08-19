import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./layout/AppShell";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { AuthProvider } from "./auth/AuthContext";
import { RecordFormPage, RecordPage, RecordTypePage } from "./pages/RecordPage";
import { CalendarPage } from "./pages/CalendarPage";
import { ProfileAccountPage, ProfileBodyPage, ProfilePage, ProfileReportsPage } from "./pages/ProfilePage";
import { RecordDetailPage } from "./pages/RecordDetailPage";
import { CmsLayout } from "./pages/CmsLayout";
import { CmsOverviewPage } from "./pages/CmsOverviewPage";
import { CmsPage } from "./pages/CmsPage";
import { CmsReportsPage } from "./pages/CmsReportsPage";
import { CmsApiKeysPage } from "./pages/CmsApiKeysPage";
import { CmsAiCallsPage } from "./pages/CmsAiCallsPage";
import { CmsUserDetailPage, CmsUserPickPage } from "./pages/CmsUserDetailPage";
import { ReportPage } from "./pages/ReportPage";
import { SharePage } from "./pages/SharePage";

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/cms" element={<CmsLayout />}>
          <Route index element={<CmsOverviewPage />} />
          <Route path="accounts" element={<CmsPage />} />
          <Route path="users" element={<CmsUserPickPage />} />
          <Route path="users/:userId" element={<CmsUserDetailPage />} />
          <Route path="reports" element={<CmsReportsPage />} />
          <Route path="api-keys" element={<CmsApiKeysPage />} />
          <Route path="ai-calls" element={<CmsAiCallsPage />} />
        </Route>
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
                <Route path="/calendar/share" element={<SharePage />} />
                <Route path="/calendar/trends" element={<Navigate to="/profile/body" replace />} />
                <Route path="/calendar/records/:id" element={<RecordDetailPage />} />
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="/profile/body" element={<ProfileBodyPage />} />
                <Route path="/profile/account" element={<ProfileAccountPage />} />
                <Route path="/profile/reports" element={<ProfileReportsPage />} />
              </Routes>
            </AppShell>
          }
        />
      </Routes>
    </AuthProvider>
  );
}
