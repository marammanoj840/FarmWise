import type { ReactNode } from "react";
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

// Public & Auth Pages
import { HomePage } from "@/pages/public/HomePage";
import { AboutPage } from "@/pages/public/AboutPage";
import { FeaturesPage } from "@/pages/public/FeaturesPage";
import { HowItWorksPage } from "@/pages/public/HowItWorksPage";
import { SolutionsPage } from "@/pages/public/SolutionsPage";
import { ResourcesPage } from "@/pages/public/ResourcesPage";
import { ContactPage } from "@/pages/public/ContactPage";
import { FaqPage } from "@/pages/public/FaqPage";
import { PrivacyPage, TermsPage } from "@/pages/public/LegalPages";
import {
  ForgotPasswordPage,
  LoginPage,
  RegisterPage,
  ResetPasswordPage,
  VerifyEmailPage,
} from "@/pages/public/AuthPages";

// Farmer Portal
import { FarmerDashboardPage } from "@/pages/farmer/FarmerDashboardPage";
import { FarmsPage } from "@/pages/farmer/FarmsPage";
import { FarmDetailPage } from "@/pages/farmer/FarmDetailPage";
import { CropsPage } from "@/pages/farmer/CropsPage";
import { CultivationPage } from "@/pages/farmer/CultivationPage";
import { InventoryPage } from "@/pages/farmer/InventoryPage";
import { FinancePage } from "@/pages/farmer/FinancePage";
import { ReportsPage } from "@/pages/farmer/ReportsPage";
import { WeatherPage } from "@/pages/farmer/WeatherPage";
import { SoilIntelligencePage } from "@/pages/farmer/SoilIntelligencePage";
import { AssistantPage } from "@/pages/farmer/AssistantPage";
import { ProducePage } from "@/pages/farmer/ProducePage";

// Shared & Role Portals
import { MarketPricesPage } from "@/pages/shared/MarketPricesPage";
import { MarketplacePage } from "@/pages/shared/MarketplacePage";
import { ExpertsDirectoryPage } from "@/pages/shared/ExpertsDirectoryPage";
import { NotificationsPage } from "@/pages/shared/NotificationsPage";
import { ProfileSettingsPage } from "@/pages/shared/ProfileSettingsPage";
import { ExpertPortalPage } from "@/pages/roles/ExpertPortalPage";
import { SupplierPortalPage } from "@/pages/roles/SupplierPortalPage";
import { BuyerPortalPage } from "@/pages/roles/BuyerPortalPage";

// Admin Portal
import { AdminDashboardPage } from "@/pages/admin/AdminDashboardPage";
import { AdminUsersPage } from "@/pages/admin/AdminUsersPage";
import { AdminModerationPage } from "@/pages/admin/AdminModerationPage";
import { AdminAuditHealthPage } from "@/pages/admin/AdminAuditHealthPage";

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
                  <AppRoutes />
                </BrowserRouter>
              </AuthProvider>
            </ToastProvider>
          </LocaleProvider>
        </ThemeProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}
