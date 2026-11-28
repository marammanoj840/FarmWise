import { useState } from "react";
import { Link, NavLink, Outlet } from "react-router-dom";
import { Menu, X } from "lucide-react";
import { BrandLogo } from "@/components/shared/BrandLogo";
import { ThemeToggle } from "@/components/shared/ThemeToggle";
import { Button } from "@/components/ui/Button";
import { getHomePathForRole, useAuth } from "@/context/AuthContext";
import { useLocale } from "@/utils/i18n";
import type { SupportedLocale } from "@/types/api";

const NAV_ITEMS = [
  { to: "/", label: "Home" },
  { to: "/features", label: "Features" },
  { to: "/how-it-works", label: "How It Works" },
  { to: "/solutions", label: "Solutions" },
  { to: "/marketplace", label: "Marketplace" },
  { to: "/resources", label: "Resources" },
  { to: "/about", label: "About" },
  { to: "/contact", label: "Contact" },
];

export function PublicLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const { isAuthenticated, user } = useAuth();
  const { locale, setLocale } = useLocale();

  return (
    <div className="flex min-h-screen flex-col bg-[#F7F5EE] text-[#11211B] dark:bg-[#081410] dark:text-[#EEF4F0]">
      <header className="sticky top-0 z-40 border-b border-[#DCE5DC]/80 bg-[#F7F5EE]/90 backdrop-blur-md dark:border-[#203D31]/80 dark:bg-[#081410]/90">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-3 sm:px-6 lg:px-8">
          <BrandLogo />

          <nav className="hidden items-center gap-1 lg:flex" aria-label="Primary navigation">
            {NAV_ITEMS.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === "/"}
                className={({ isActive }) =>
                  `whitespace-nowrap rounded-lg px-2.5 py-2 text-sm font-medium transition xl:px-3 ${
                    isActive
                      ? "bg-[#10402E]/10 text-[#10402E] dark:bg-[#3FB973]/15 dark:text-[#56CF88]"
                      : "text-[#4B6358] hover:text-[#11211B] dark:text-[#9EB7AB] dark:hover:text-white"
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <div className="hidden shrink-0 items-center gap-2 lg:flex xl:gap-2.5">
            <select
              aria-label="Select language"
              value={locale}
              onChange={(e) => setLocale(e.target.value as SupportedLocale)}
              className="h-10 rounded-xl border border-[#DCE5DC] bg-white px-2.5 text-xs font-semibold text-[#11211B] dark:border-[#244337] dark:bg-[#0F211A] dark:text-[#EEF4F0]"
            >
              <option value="en">EN (INR ₹)</option>
              <option value="hi">HI (Preview)</option>
            </select>
            <ThemeToggle />
            {isAuthenticated && user ? (
              <Link to={getHomePathForRole(user.primaryRole)}>
                <Button size="sm">Open {user.primaryRole.toLowerCase()} portal</Button>
              </Link>
            ) : (
              <>
                <Link to="/login">
                  <Button variant="ghost" size="sm">
                    Sign In
                  </Button>
                </Link>
                <Link to="/register">
                  <Button size="sm">Get Started</Button>
                </Link>
              </>
            )}
          </div>

          <div className="flex items-center gap-2 lg:hidden">
            <ThemeToggle />
            <button
              type="button"
              onClick={() => setMobileOpen((o) => !o)}
              aria-label="Toggle menu"
              className="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-[#DCE5DC] bg-white dark:border-[#244337] dark:bg-[#0F211A]"
            >
              {mobileOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
            </button>
          </div>
        </div>

        {mobileOpen && (
          <div className="border-t border-[#DCE5DC] bg-white px-4 py-4 dark:border-[#203D31] dark:bg-[#0F211A] lg:hidden">
            <nav className="flex flex-col gap-1">
              {NAV_ITEMS.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === "/"}
                  onClick={() => setMobileOpen(false)}
                  className={({ isActive }) =>
                    `rounded-xl px-3.5 py-2.5 text-sm font-medium ${
                      isActive
                        ? "bg-[#10402E]/10 text-[#10402E] dark:bg-[#3FB973]/15 dark:text-[#56CF88]"
                        : "text-[#4B6358] dark:text-[#9EB7AB]"
                    }`
                  }
                >
                  {item.label}
                </NavLink>
              ))}
            </nav>
            <div className="mt-4 flex flex-col gap-2 border-t border-[#DCE5DC] pt-4 dark:border-[#203D31]">
              {isAuthenticated && user ? (
                <Link to={getHomePathForRole(user.primaryRole)} onClick={() => setMobileOpen(false)}>
                  <Button className="w-full">Go to Portal</Button>
                </Link>
              ) : (
                <>
                  <Link to="/login" onClick={() => setMobileOpen(false)}>
                    <Button variant="outline" className="w-full">
                      Sign In
                    </Button>
                  </Link>
                  <Link to="/register" onClick={() => setMobileOpen(false)}>
                    <Button className="w-full">Create Free Account</Button>
                  </Link>
                </>
              )}
            </div>
          </div>
        )}
      </header>

      <main className="flex-1">
        <Outlet />
      </main>

      <footer className="border-t border-[#DCE5DC] bg-[#10291F] text-[#DCE8E0] dark:border-[#1E3B2F] dark:bg-[#060F0C]">
        <div className="mx-auto grid max-w-7xl gap-8 px-4 py-12 sm:px-6 md:grid-cols-4 lg:px-8">
          <div className="space-y-3">
            <div className="font-display text-xl font-bold text-white">FarmWise</div>
            <p className="text-sm leading-relaxed text-[#A7C0B3]">
              Smarter Farming. Better Harvests. Stronger Futures. Built for Indian agriculture with transparent data,
              agronomic records, and direct market connectivity.
            </p>
            <p className="text-xs text-[#7E9E8F]">Default Currency: INR (₹) • Localization Ready</p>
          </div>
          <div>
            <h4 className="mb-3 text-sm font-semibold uppercase tracking-wider text-white">Platform</h4>
            <ul className="space-y-2 text-sm text-[#B7CFC3]">
              <li><Link to="/features" className="hover:text-white">Features</Link></li>
              <li><Link to="/how-it-works" className="hover:text-white">How It Works</Link></li>
              <li><Link to="/solutions" className="hover:text-white">Ecosystem Solutions</Link></li>
              <li><Link to="/marketplace" className="hover:text-white">Agri Marketplace</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="mb-3 text-sm font-semibold uppercase tracking-wider text-white">Knowledge & Support</h4>
            <ul className="space-y-2 text-sm text-[#B7CFC3]">
              <li><Link to="/resources" className="hover:text-white">Farmer Resources</Link></li>
              <li><Link to="/faq" className="hover:text-white">Frequently Asked Questions</Link></li>
              <li><Link to="/about" className="hover:text-white">About FarmWise</Link></li>
              <li><Link to="/contact" className="hover:text-white">Contact Us</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="mb-3 text-sm font-semibold uppercase tracking-wider text-white">Legal & Portals</h4>
            <ul className="space-y-2 text-sm text-[#B7CFC3]">
              <li><Link to="/login" className="hover:text-white">Portal Sign In</Link></li>
              <li><Link to="/register" className="hover:text-white">Register Account</Link></li>
              <li><Link to="/privacy" className="hover:text-white">Privacy Policy</Link></li>
              <li><Link to="/terms" className="hover:text-white">Terms & Conditions</Link></li>
            </ul>
          </div>
        </div>
        <div className="border-t border-white/10 px-4 py-5 text-center text-xs text-[#8AA799]">
          © {new Date().getFullYear()} FarmWise Agricultural Technology Platform. Built with Spring Boot & React.
        </div>
      </footer>
    </div>
  );
}
