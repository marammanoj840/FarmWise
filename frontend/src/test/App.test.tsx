import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider } from "@/context/AuthContext";
import { ThemeProvider } from "@/context/ThemeContext";
import { ToastProvider } from "@/context/ToastContext";
import { LocaleProvider } from "@/utils/i18n";
import { AppRoutes } from "@/App";
import { formatCurrencyINR } from "@/utils/format";

vi.mock("@/api/services", async (importOriginal) => {
  const actual = await importOriginal<typeof import("@/api/services")>();
  return {
    ...actual,
    publicApi: {
      overview: vi.fn().mockResolvedValue({
        supportedCrops: 10,
        productCategories: 7,
        approvedProducts: 6,
        registeredExperts: 2,
        defaultCurrency: "INR",
      }),
      submitContact: vi.fn().mockResolvedValue({ message: "Saved", timestamp: new Date().toISOString() }),
    },
    soilApi: {
      ...actual.soilApi,
      knowledgeBase: vi.fn().mockResolvedValue([]),
    },
    cropApi: {
      ...actual.cropApi,
      catalog: vi.fn().mockResolvedValue([]),
    },
  };
});

function renderWithProviders(initialPath = "/") {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={client}>
      <ThemeProvider>
        <LocaleProvider>
          <ToastProvider>
            <AuthProvider>
              <MemoryRouter initialEntries={[initialPath]}>
                <AppRoutes />
              </MemoryRouter>
            </AuthProvider>
          </ToastProvider>
        </LocaleProvider>
      </ThemeProvider>
    </QueryClientProvider>,
  );
}

describe("FarmWise Frontend Core Flows", () => {
  it("formats Indian Rupee amounts accurately", () => {
    expect(formatCurrencyINR(148500)).toBe("₹1,48,500");
    expect(formatCurrencyINR(266.5)).toBe("₹266.50");
  });

  it("renders public homepage with tagline, fallback 3D/SVG hero, and navigation", async () => {
    renderWithProviders("/");
    expect(
      await screen.findByRole("heading", { name: /Smarter Farming\. Better Harvests\. Stronger Futures\./i }),
    ).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /Explore Marketplace/i })).toBeInTheDocument();
  });

  it("toggles dark mode and opens login page cleanly", async () => {
    const user = userEvent.setup();
    renderWithProviders("/login");
    expect(await screen.findByText(/Sign in to FarmWise/i)).toBeInTheDocument();

    const [themeBtn] = screen.getAllByRole("button", { name: /Switch to dark mode/i });
    await user.click(themeBtn);
    expect(document.documentElement.classList.contains("dark")).toBe(true);
  });

  it("redirects unauthenticated access to protected /app/farmer route to /login", async () => {
    renderWithProviders("/app/farmer");
    expect(await screen.findByText(/Sign in to FarmWise/i)).toBeInTheDocument();
  });
});
