import { Component, type ErrorInfo, type ReactNode } from "react";
import { AlertTriangle, ChevronLeft, ChevronRight, Home } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/Button";

export function Skeleton({ className = "h-5 w-full" }: { className?: string }) {
  return <div className={`animate-pulse rounded-lg bg-[#E4ECE4] dark:bg-[#193328] ${className}`} />;
}

export function EmptyState({
  icon,
  title,
  description,
  action,
}: {
  icon?: ReactNode;
  title: string;
  description: string;
  action?: ReactNode;
}) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-[#C9D6CA] bg-[#FBFBF8] px-6 py-12 text-center dark:border-[#244337] dark:bg-[#0D1E17]">
      {icon && (
        <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-[#EEF3EE] text-[#10402E] dark:bg-[#173026] dark:text-[#4FD085]">
          {icon}
        </div>
      )}
      <h4 className="text-base font-semibold text-[#11211B] dark:text-[#EEF4F0]">{title}</h4>
      <p className="mt-1 max-w-md text-sm text-[#4B6358] dark:text-[#9EB7AB]">{description}</p>
      {action && <div className="mt-5">{action}</div>}
    </div>
  );
}

export function Pagination({
  page,
  totalPages,
  onPageChange,
}: {
  page: number;
  totalPages: number;
  onPageChange: (nextPage: number) => void;
}) {
  if (totalPages <= 1) return null;
  return (
    <div className="mt-4 flex items-center justify-between gap-4 border-t border-[#DCE5DC] pt-4 dark:border-[#203D31]">
      <span className="text-xs font-medium text-[#4B6358] dark:text-[#9EB7AB]">
        Page {page + 1} of {totalPages}
      </span>
      <div className="flex items-center gap-2">
        <Button
          size="sm"
          variant="outline"
          disabled={page <= 0}
          onClick={() => onPageChange(page - 1)}
          icon={<ChevronLeft className="h-4 w-4" />}
        >
          Prev
        </Button>
        <Button
          size="sm"
          variant="outline"
          disabled={page + 1 >= totalPages}
          onClick={() => onPageChange(page + 1)}
        >
          Next
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}

export function Breadcrumbs({ items }: { items: { label: string; to?: string }[] }) {
  return (
    <nav aria-label="Breadcrumb" className="mb-4 flex flex-wrap items-center gap-1.5 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
      <Link to="/" className="inline-flex items-center gap-1 hover:text-[#10402E] dark:hover:text-[#4FD085]">
        <Home className="h-3.5 w-3.5" />
      </Link>
      {items.map((item, i) => (
        <span key={item.label} className="inline-flex items-center gap-1.5">
          <span>/</span>
          {item.to && i < items.length - 1 ? (
            <Link to={item.to} className="hover:underline">
              {item.label}
            </Link>
          ) : (
            <span className="font-semibold text-[#11211B] dark:text-[#EEF4F0]">{item.label}</span>
          )}
        </span>
      ))}
    </nav>
  );
}

export function LoadingScreen({ label = "Loading FarmWise..." }: { label?: string }) {
  return (
    <div className="flex min-h-[50vh] flex-col items-center justify-center gap-3 p-8">
      <div className="h-10 w-10 animate-spin rounded-full border-4 border-[#DCE5DC] border-t-[#10402E] dark:border-[#203D31] dark:border-t-[#4FD085]" />
      <p className="text-sm font-medium text-[#4B6358] dark:text-[#9EB7AB]">{label}</p>
    </div>
  );
}

interface ErrorBoundaryState {
  hasError: boolean;
  message: string;
}

export class ErrorBoundary extends Component<{ children: ReactNode }, ErrorBoundaryState> {
  state: ErrorBoundaryState = { hasError: false, message: "" };

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, message: error.message };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("FarmWise UI error boundary caught:", error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex min-h-screen items-center justify-center bg-[#F7F5EE] p-6 dark:bg-[#081410]">
          <div className="fw-card max-w-md p-6 text-center">
            <AlertTriangle className="mx-auto mb-3 h-10 w-10 text-amber-500" />
            <h2 className="text-xl font-bold">Something went wrong</h2>
            <p className="mt-2 text-sm text-[#4B6358] dark:text-[#9EB7AB]">
              {this.state.message || "An unexpected interface error occurred."}
            </p>
            <Button className="mt-5" onClick={() => window.location.reload()}>
              Reload Application
            </Button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}
