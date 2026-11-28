import { lazy, Suspense, type ReactNode } from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider, getHomePathForRole, useAuth } from "@/context/AuthContext";
import { ThemeProvider } from "@/context/ThemeContext";
import { ToastProvider } from "@/context/ToastContext";
import { LocaleProvider } from "@/utils/i18n";
import { ErrorBoundary, LoadingScreen } from "@/components/ui/Feedback";
import { PublicLayout } from "@/layouts/PublicLayout";
import { PortalLayout } from "@/layouts/PortalLayout";
import type { UserRole } from "@/types/api";

// Public & Auth Pages (Eager load critical landing, lazy load auth & subpages)
import { HomePage } from "@/pages/public/HomePage";

const AboutPage = lazy(() => import("@/pages/public/AboutPage").then((m) => ({ default: m.AboutPage })));
const FeaturesPage = lazy(() => import("@/pages/public/FeaturesPage").then((m) => ({ default: m.FeaturesPage })));
const HowItWorksPage = lazy(() => import("@/pages/public/HowItWorksPage").then((m) => ({ default: m.HowItWorksPage })));
const SolutionsPage = lazy(() => import("@/pages/public/SolutionsPage").then((m) => ({ default: m.SolutionsPage })));
const ResourcesPage = lazy(() => import("@/pages/public/ResourcesPage").then((m) => ({ default: m.ResourcesPage })));
const ContactPage = lazy(() => import("@/pages/public/ContactPage").then((m) => ({ default: m.ContactPage })));
const FaqPage = lazy(() => import("@/pages/public/FaqPage").then((m) => ({ default: m.FaqPage })));
const PrivacyPage = lazy(() => import("@/pages/public/LegalPages").then((m) => ({ default: m.PrivacyPage })));
const TermsPage = lazy(() => import("@/pages/public/LegalPages").then((m) => ({ default: m.TermsPage })));
const LoginPage = lazy(() => import("@/pages/public/AuthPages").then((m) => ({ default: m.LoginPage })));
const RegisterPage = lazy(() => import("@/pages/public/AuthPages").then((m) => ({ default: m.RegisterPage })));
const ForgotPasswordPage = lazy(() => import("@/pages/public/AuthPages").then((m) => ({ default: m.ForgotPasswordPage })));
const ResetPasswordPage = lazy(() => import("@/pages/public/AuthPages").then((m) => ({ default: m.ResetPasswordPage })));
const VerifyEmailPage = lazy(() => import("@/pages/public/AuthPages").then((m) => ({ default: m.VerifyEmailPage })));

// Farmer Portal (Lazy-loaded per module)
const FarmerDashboardPage = lazy(() => import("@/pages/farmer/FarmerDashboardPage").then((m) => ({ default: m.FarmerDashboardPage })));
const FarmsPage = lazy(() => import("@/pages/farmer/FarmsPage").then((m) => ({ default: m.FarmsPage })));
const FarmDetailPage = lazy(() => import("@/pages/farmer/FarmDetailPage").then((m) => ({ default: m.FarmDetailPage })));
const CropsPage = lazy(() => import("@/pages/farmer/CropsPage").then((m) => ({ default: m.CropsPage })));
const CultivationPage = lazy(() => import("@/pages/farmer/CultivationPage").then((m) => ({ default: m.CultivationPage })));
const InventoryPage = lazy(() => import("@/pages/farmer/InventoryPage").then((m) => ({ default: m.InventoryPage })));
const FinancePage = lazy(() => import("@/pages/farmer/FinancePage").then((m) => ({ default: m.FinancePage })));
const ReportsPage = lazy(() => import("@/pages/farmer/ReportsPage").then((m) => ({ default: m.ReportsPage })));
const WeatherPage = lazy(() => import("@/pages/farmer/WeatherPage").then((m) => ({ default: m.WeatherPage })));
const SoilIntelligencePage = lazy(() => import("@/pages/farmer/SoilIntelligencePage").then((m) => ({ default: m.SoilIntelligencePage })));
const AssistantPage = lazy(() => import("@/pages/farmer/AssistantPage").then((m) => ({ default: m.AssistantPage })));
const ProducePage = lazy(() => import("@/pages/farmer/ProducePage").then((m) => ({ default: m.ProducePage })));

// Shared & Role Portals
const MarketPricesPage = lazy(() => import("@/pages/shared/MarketPricesPage").then((m) => ({ default: m.MarketPricesPage })));
const MarketplacePage = lazy(() => import("@/pages/shared/MarketplacePage").then((m) => ({ default: m.MarketplacePage })));
const ExpertsDirectoryPage = lazy(() => import("@/pages/shared/ExpertsDirectoryPage").then((m) => ({ default: m.ExpertsDirectoryPage })));
const NotificationsPage = lazy(() => import("@/pages/shared/NotificationsPage").then((m) => ({ default: m.NotificationsPage })));
const ProfileSettingsPage = lazy(() => import("@/pages/shared/ProfileSettingsPage").then((m) => ({ default: m.ProfileSettingsPage })));
const ExpertPortalPage = lazy(() => import("@/pages/roles/ExpertPortalPage").then((m) => ({ default: m.ExpertPortalPage })));
const SupplierPortalPage = lazy(() => import("@/pages/roles/SupplierPortalPage").then((m) => ({ default: m.SupplierPortalPage })));
const BuyerPortalPage = lazy(() => import("@/pages/roles/BuyerPortalPage").then((m) => ({ default: m.BuyerPortalPage })));

// Admin Portal
const AdminDashboardPage = lazy(() => import("@/pages/admin/AdminDashboardPage").then((m) => ({ default: m.AdminDashboardPage })));
const AdminUsersPage = lazy(() => import("@/pages/admin/AdminUsersPage").then((m) => ({ default: m.AdminUsersPage })));
const AdminModerationPage = lazy(() => import("@/pages/admin/AdminModerationPage").then((m) => ({ default: m.AdminModerationPage })));
const AdminAuditHealthPage = lazy(() => import("@/pages/admin/AdminAuditHealthPage").then((m) => ({ default: m.AdminAuditHealthPage })));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function ProtectedRoute({
  children,
  allowedRoles,
}: {
  children: ReactNode;
  allowedRoles?: UserRole[];
}) {
  const { isAuthenticated, isLoading, user, hasRole } = useAuth();
  if (isLoading) return <LoadingScreen />;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (allowedRoles && !hasRole(...allowedRoles)) {
    return <Navigate to={getHomePathForRole(user?.primaryRole)} replace />;
  }
  return <>{children}</>;
}

function RoleRedirect() {
  const { user } = useAuth();
  return <Navigate to={getHomePathForRole(user?.primaryRole)} replace />;
}

export function AppRoutes() {
  return (
    <Routes>
      {/* Public Website */}
      <Route element={<PublicLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path="/features" element={<FeaturesPage />} />
        <Route path="/how-it-works" element={<HowItWorksPage />} />
        <Route path="/solutions" element={<SolutionsPage />} />
        <Route path="/resources" element={<ResourcesPage />} />
        <Route path="/marketplace" element={<MarketplacePage />} />
        <Route path="/contact" element={<ContactPage />} />
        <Route path="/faq" element={<FaqPage />} />
        <Route path="/privacy" element={<PrivacyPage />} />
        <Route path="/terms" element={<TermsPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />
      </Route>

      {/* Authenticated Role Portals */}
      <Route
        path="/app"
        element={
          <ProtectedRoute>
            <PortalLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<RoleRedirect />} />
        <Route
          path="farmer"
          element={
            <ProtectedRoute allowedRoles={["FARMER", "ADMIN"]}>
              <FarmerDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="farms"
          element={
            <ProtectedRoute allowedRoles={["FARMER", "ADMIN"]}>
              <FarmsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="farms/:id"
          element={
            <ProtectedRoute allowedRoles={["FARMER", "ADMIN"]}>
              <FarmDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="crops"
          element={
            <ProtectedRoute allowedRoles={["FARMER", "ADMIN"]}>
              <CropsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="cultivation"
          element={
            <ProtectedRoute allowedRoles={["FARMER", "ADMIN"]}>
              <CultivationPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="inventory"
          element={
            <ProtectedRoute allowedRoles={["FARMER", "ADMIN"]}>
              <InventoryPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="finance"
          element={
            <ProtectedRoute allowedRoles={["FARMER", "ADMIN"]}>
              <FinancePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="reports"
          element={
            <ProtectedRoute allowedRoles={["FARMER", "ADMIN"]}>
              <ReportsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="weather"
          element={
            <ProtectedRoute allowedRoles={["FARMER", "EXPERT", "ADMIN"]}>
              <WeatherPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="soil-intelligence"
          element={
            <ProtectedRoute allowedRoles={["FARMER", "EXPERT", "ADMIN"]}>
              <SoilIntelligencePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="assistant"
          element={
            <ProtectedRoute allowedRoles={["FARMER", "EXPERT", "ADMIN"]}>
              <AssistantPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="produce"
          element={
            <ProtectedRoute allowedRoles={["FARMER", "ADMIN"]}>
              <ProducePage />
            </ProtectedRoute>
          }
        />

        <Route path="market-prices" element={<MarketPricesPage />} />
        <Route path="experts" element={<ExpertsDirectoryPage />} />
        <Route path="marketplace" element={<MarketplacePage />} />
        <Route path="notifications" element={<NotificationsPage />} />
        <Route path="profile" element={<ProfileSettingsPage />} />

        <Route
          path="expert"
          element={
            <ProtectedRoute allowedRoles={["EXPERT", "ADMIN"]}>
              <ExpertPortalPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="supplier"
          element={
            <ProtectedRoute allowedRoles={["SUPPLIER", "ADMIN"]}>
              <SupplierPortalPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="buyer"
          element={
            <ProtectedRoute allowedRoles={["BUYER", "ADMIN"]}>
              <BuyerPortalPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="admin"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="admin/users"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminUsersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="admin/moderation"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminModerationPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="admin/audit-logs"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminAuditHealthPage />
            </ProtectedRoute>
          }
        />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider>
          <LocaleProvider>
            <ToastProvider>
              <AuthProvider>
                <BrowserRouter>
                  <Suspense fallback={<LoadingScreen />}>
                    <AppRoutes />
                  </Suspense>
                </BrowserRouter>
              </AuthProvider>
            </ToastProvider>
          </LocaleProvider>
        </ThemeProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}
