import { Moon, Sun } from "lucide-react";
import { useTheme } from "@/context/ThemeContext";

export function ThemeToggle() {
  const { theme, toggleTheme } = useTheme();
  return (
    <button
      type="button"
      onClick={toggleTheme}
      aria-label={theme === "dark" ? "Switch to light mode" : "Switch to dark mode"}
      title={theme === "dark" ? "Light mode" : "Dark mode"}
      className="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-[#DCE5DC] bg-white text-[#11211B] transition hover:bg-[#EEF3EE] dark:border-[#244337] dark:bg-[#0F211A] dark:text-[#EEF4F0] dark:hover:bg-[#173026]"
    >
      {theme === "dark" ? <Sun className="h-4 w-4 text-amber-400" /> : <Moon className="h-4 w-4 text-[#10402E]" />}
    </button>
  );
}
