import type { ButtonHTMLAttributes, ReactNode } from "react";
import { Loader2 } from "lucide-react";

type Variant = "primary" | "secondary" | "outline" | "ghost" | "danger";
type Size = "sm" | "md" | "lg";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
  loading?: boolean;
  icon?: ReactNode;
}

const VARIANT_CLASSES: Record<Variant, string> = {
  primary:
    "bg-[#10402E] text-white hover:bg-[#17573F] focus-visible:ring-[#2A8C55] dark:bg-[#2A8C55] dark:hover:bg-[#36A264]",
  secondary:
    "bg-[#EEF3EE] text-[#10402E] hover:bg-[#DFECE0] focus-visible:ring-[#2A8C55] dark:bg-[#173026] dark:text-[#E5F2EA] dark:hover:bg-[#1F4133]",
  outline:
    "border border-[#C9D6CA] bg-transparent text-[#11211B] hover:bg-[#EEF3EE] focus-visible:ring-[#2A8C55] dark:border-[#254538] dark:text-[#E5F2EA] dark:hover:bg-[#152B22]",
  ghost:
    "bg-transparent text-[#11211B] hover:bg-[#EEF3EE] focus-visible:ring-[#2A8C55] dark:text-[#DDEAE2] dark:hover:bg-[#152B22]",
  danger:
    "bg-red-600 text-white hover:bg-red-700 focus-visible:ring-red-500 dark:bg-red-600 dark:hover:bg-red-500",
};

const SIZE_CLASSES: Record<Size, string> = {
  sm: "min-h-9 px-3 py-1.5 text-xs gap-1.5",
  md: "min-h-11 px-4 py-2.5 text-sm gap-2",
  lg: "min-h-12 px-6 py-3 text-base gap-2.5",
};

export function Button({
  variant = "primary",
  size = "md",
  loading = false,
  disabled,
  icon,
  children,
  className = "",
  type = "button",
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      disabled={disabled || loading}
      className={`inline-flex items-center justify-center rounded-xl font-semibold transition-all duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-55 ${VARIANT_CLASSES[variant]} ${SIZE_CLASSES[size]} ${className}`}
      {...props}
    >
      {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : icon}
      {children}
    </button>
  );
}
