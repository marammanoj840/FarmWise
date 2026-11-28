import { useState, type ReactNode } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import {
  BarChart3,
  Bell,
  Bot,
  CalendarCheck,
  CloudSun,
  FileSpreadsheet,
  FlaskConical,
  IndianRupee,
  LayoutDashboard,
  LogOut,
  MapPin,
  Menu,
  Package,
  Shield,
  ShoppingBag,
  Sprout,
  Store,
  TrendingUp,
  UserCircle,
  Users,
  Wheat,
  X,
} from "lucide-react";
import { notificationApi } from "@/api/services";
import { BrandLogo } from "@/components/shared/BrandLogo";
import { ThemeToggle } from "@/components/shared/ThemeToggle";
import { useAuth } from "@/context/AuthContext";
import type { UserRole } from "@/types/api";

interface NavItem {
  to: string;
  label: string;
  icon: ReactNode;
  roles?: UserRole[];
}

const PORTAL_NAV: NavItem[] = [
  { to: "/app/farmer", label: "Farmer Dashboard", icon: <LayoutDashboard className="h-4 w-4" />, roles: ["FARMER", "ADMIN"] },
  { to: "/app/farms", label: "My Farms", icon: <MapPin className="h-4 w-4" />, roles: ["FARMER", "ADMIN"] },
  { to: "/app/crops", label: "Crops & Harvests", icon: <Sprout className="h-4 w-4" />, roles: ["FARMER", "ADMIN"] },
  { to: "/app/cultivation", label: "Cultivation", icon: <CalendarCheck className="h-4 w-4" />, roles: ["FARMER", "ADMIN"] },
  { to: "/app/inventory", label: "Inventory", icon: <Package className="h-4 w-4" />, roles: ["FARMER", "ADMIN"] },
  { to: "/app/finance", label: "Finance & P&L", icon: <IndianRupee className="h-4 w-4" />, roles: ["FARMER", "ADMIN"] },
  { to: "/app/reports", label: "Reports (PDF/CSV)", icon: <FileSpreadsheet className="h-4 w-4" />, roles: ["FARMER", "ADMIN"] },
  { to: "/app/weather", label: "Weather", icon: <CloudSun className="h-4 w-4" />, roles: ["FARMER", "EXPERT", "ADMIN"] },
  { to: "/app/soil-intelligence", label: "Soil & Crop Lab", icon: <FlaskConical className="h-4 w-4" />, roles: ["FARMER", "EXPERT", "ADMIN"] },
  { to: "/app/assistant", label: "AI Assistant", icon: <Bot className="h-4 w-4" />, roles: ["FARMER", "EXPERT", "ADMIN"] },
  { to: "/app/produce", label: "My Produce Sales", icon: <Wheat className="h-4 w-4" />, roles: ["FARMER", "ADMIN"] },
  { to: "/app/market-prices", label: "Mandi Prices", icon: <TrendingUp className="h-4 w-4" /> },
  { to: "/app/experts", label: "Expert Advisory", icon: <Users className="h-4 w-4" /> },
  { to: "/app/marketplace", label: "Marketplace", icon: <ShoppingBag className="h-4 w-4" /> },
  { to: "/app/expert", label: "Expert Workspace", icon: <Users className="h-4 w-4" />, roles: ["EXPERT", "ADMIN"] },
  { to: "/app/supplier", label: "Supplier Portal", icon: <Store className="h-4 w-4" />, roles: ["SUPPLIER", "ADMIN"] },
  { to: "/app/buyer", label: "Buyer Portal", icon: <ShoppingBag className="h-4 w-4" />, roles: ["BUYER", "ADMIN"] },
  { to: "/app/admin", label: "Admin Overview", icon: <Shield className="h-4 w-4" />, roles: ["ADMIN"] },
  { to: "/app/admin/users", label: "Users & Roles", icon: <Users className="h-4 w-4" />, roles: ["ADMIN"] },
  { to: "/app/admin/moderation", label: "Moderation", icon: <BarChart3 className="h-4 w-4" />, roles: ["ADMIN"] },
  { to: "/app/admin/audit-logs", label: "Audit & Health", icon: <Shield className="h-4 w-4" />, roles: ["ADMIN"] },
  { to: "/app/notifications", label: "Notifications", icon: <Bell className="h-4 w-4" /> },
  { to: "/app/profile", label: "Profile & Settings", icon: <UserCircle className="h-4 w-4" /> },
];

export function PortalLayout() {
  const { user, logout, hasRole } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const navigate = useNavigate();

  const { data: unread } = useQuery({
    queryKey: ["notifications", "unread-count"],
    queryFn: notificationApi.unreadCount,
    refetchInterval: 30000,
  });

  const visibleLinks = PORTAL_NAV.filter((item) => !item.roles || hasRole(...item.roles));

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  const navContent = (
    <div className="flex h-full flex-col">
      <div className="flex items-center justify-between border-b border-[#DCE5DC] px-4 py-3.5 dark:border-[#203D31]">
        <BrandLogo to="/" />
        <button
          type="button"
          onClick={() => setSidebarOpen(false)}
          className="rounded-lg p-1.5 lg:hidden"
          aria-label="Close sidebar"
        >
          <X className="h-5 w-5" />
        </button>
      </div>

      <div className="flex-1 space-y-1 overflow-y-auto px-3 py-4">
        {visibleLinks.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === "/app/admin" || item.to === "/app/farmer"}
            onClick={() => setSidebarOpen(false)}
            className={({ isActive }) =>
              `flex items-center justify-between gap-2.5 rounded-xl px-3 py-2.5 text-sm font-medium transition ${
                isActive
                  ? "bg-[#10402E] text-white shadow-xs dark:bg-[#2A8C55]"
                  : "text-[#355245] hover:bg-[#EEF3EE] dark:text-[#AFC6BA] dark:hover:bg-[#152B22]"
              }`
            }
          >
            <span className="flex items-center gap-2.5">
              {item.icon}
              <span>{item.label}</span>
            </span>
            {item.to === "/app/notifications" && unread && unread.unreadCount > 0 ? (
              <span className="rounded-full bg-amber-500 px-2 py-0.5 text-[10px] font-bold text-white">
                {unread.unreadCount}
              </span>
            ) : null}
          </NavLink>
        ))}
      </div>

      <div className="border-t border-[#DCE5DC] p-3 dark:border-[#203D31]">
        <div className="mb-2.5 rounded-xl bg-[#EEF3EE] p-3 dark:bg-[#152B22]">
          <p className="truncate text-sm font-semibold">{user?.fullName}</p>
          <p className="truncate text-xs text-[#4B6358] dark:text-[#9EB7AB]">
            {user?.primaryRole} • {user?.email}
          </p>
        </div>
        <button
          type="button"
          onClick={handleLogout}
          className="flex w-full items-center justify-center gap-2 rounded-xl border border-[#C9D6CA] py-2.5 text-sm font-semibold text-red-700 transition hover:bg-red-50 dark:border-[#244337] dark:text-red-400 dark:hover:bg-red-950/30"
        >
          <LogOut className="h-4 w-4" />
          Sign Out
        </button>
      </div>
    </div>
  );

  return (
    <div className="flex min-h-screen bg-[#F7F5EE] text-[#11211B] dark:bg-[#081410] dark:text-[#EEF4F0]">
      <aside className="hidden w-68 shrink-0 border-r border-[#DCE5DC] bg-white dark:border-[#203D31] dark:bg-[#0F211A] lg:block">
        <div className="sticky top-0 h-screen">{navContent}</div>
      </aside>

      {sidebarOpen && (
        <div className="fixed inset-0 z-50 flex lg:hidden">
          <div className="fixed inset-0 bg-black/50" onClick={() => setSidebarOpen(false)} />
          <aside className="relative z-10 w-72 bg-white shadow-2xl dark:bg-[#0F211A]">
            {navContent}
          </aside>
        </div>
      )}

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-30 flex items-center justify-between gap-4 border-b border-[#DCE5DC] bg-[#F7F5EE]/95 px-4 py-3 backdrop-blur-md dark:border-[#203D31] dark:bg-[#081410]/95 sm:px-6">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => setSidebarOpen(true)}
              aria-label="Open navigation menu"
              className="inline-flex h-11 w-11 items-center justify-center rounded-xl border border-[#DCE5DC] bg-white dark:border-[#244337] dark:bg-[#0F211A] lg:hidden"
            >
              <Menu className="h-5 w-5" />
            </button>
            <div className="hidden sm:block">
              <p className="text-xs font-semibold uppercase tracking-wider text-[#2A8C55] dark:text-[#6EE09B]">
                FarmWise {user?.primaryRole} Workspace
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2.5">
            <Link
              to="/"
              className="hidden rounded-xl border border-[#DCE5DC] bg-white px-3 py-2 text-xs font-semibold hover:bg-[#EEF3EE] dark:border-[#244337] dark:bg-[#0F211A] dark:hover:bg-[#173026] sm:inline-flex"
            >
              Public Website
            </Link>
            <Link
              to="/app/notifications"
              className="relative inline-flex h-10 w-10 items-center justify-center rounded-xl border border-[#DCE5DC] bg-white hover:bg-[#EEF3EE] dark:border-[#244337] dark:bg-[#0F211A]"
              aria-label="Notifications"
            >
              <Bell className="h-4 w-4" />
              {unread && unread.unreadCount > 0 ? (
                <span className="absolute -right-1 -top-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-amber-500 px-1 text-[10px] font-bold text-white">
                  {unread.unreadCount}
                </span>
              ) : null}
            </Link>
            <ThemeToggle />
          </div>
        </header>

        <main className="mx-auto w-full max-w-7xl flex-1 p-4 sm:p-6 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
