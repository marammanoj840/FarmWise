import type { ReactNode } from "react";

export function Card({
  children,
  className = "",
  padding = "p-5 sm:p-6",
}: {
  children: ReactNode;
  className?: string;
  padding?: string;
}) {
  return <div className={`fw-card ${padding} ${className}`}>{children}</div>;
}

type BadgeTone = "green" | "amber" | "red" | "blue" | "slate";

const BADGE_STYLES: Record<BadgeTone, string> = {
  green: "bg-emerald-100 text-emerald-800 dark:bg-emerald-950/70 dark:text-emerald-300 border-emerald-200 dark:border-emerald-800/70",
  amber: "bg-amber-100 text-amber-800 dark:bg-amber-950/70 dark:text-amber-300 border-amber-200 dark:border-amber-800/70",
  red: "bg-red-100 text-red-800 dark:bg-red-950/70 dark:text-red-300 border-red-200 dark:border-red-800/70",
  blue: "bg-sky-100 text-sky-800 dark:bg-sky-950/70 dark:text-sky-300 border-sky-200 dark:border-sky-800/70",
  slate: "bg-slate-100 text-slate-700 dark:bg-slate-800/80 dark:text-slate-300 border-slate-200 dark:border-slate-700",
};

export function Badge({
  children,
  tone = "green",
  className = "",
}: {
  children: ReactNode;
  tone?: BadgeTone;
  className?: string;
}) {
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-xs font-semibold ${BADGE_STYLES[tone]} ${className}`}
    >
      {children}
    </span>
  );
}
