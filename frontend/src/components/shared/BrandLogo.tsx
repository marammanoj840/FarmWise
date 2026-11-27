import { Link } from "react-router-dom";

export function BrandLogo({
  to = "/",
  compact = false,
}: {
  to?: string;
  compact?: boolean;
}) {
  return (
    <Link to={to} className="inline-flex items-center gap-2.5 focus-visible:outline-none">
      <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-[#10402E] to-[#2A8C55] text-white shadow-sm dark:from-[#2A8C55] dark:to-[#4FD085]">
        <svg viewBox="0 0 28 28" className="h-6 w-6" fill="none" aria-hidden="true">
          <path
            d="M14 23V12M14 12C14 7.5 18 4.5 23 5C23 10 19.5 13 14 12ZM14 15C14 11 10.5 8 5.5 8.5C5.5 13.5 9 16 14 15Z"
            stroke="currentColor"
            strokeWidth="2.1"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
          <path
            d="M7 22.5C9.5 21 18.5 21 21 22.5"
            stroke="#EAB049"
            strokeWidth="2.1"
            strokeLinecap="round"
          />
        </svg>
      </span>
      {!compact && (
        <span className="flex flex-col leading-none">
          <span className="font-display text-lg font-bold tracking-tight text-[#10402E] dark:text-[#EEF4F0]">
            FarmWise
          </span>
          <span className="text-[10px] font-semibold uppercase tracking-widest text-[#2A8C55] dark:text-[#6EE09B]">
            Agri Ecosystem
          </span>
        </span>
      )}
    </Link>
  );
}
