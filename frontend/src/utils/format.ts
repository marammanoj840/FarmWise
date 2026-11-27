export type SupportedCurrency = "INR";
export type SupportedLocale = "en" | "hi" | "pa" | "mr" | "ta";

export function formatCurrencyINR(amount: number | string | null | undefined): string {
  const num = typeof amount === "string" ? Number(amount) : (amount ?? 0);
  if (Number.isNaN(num)) {
    return "₹0";
  }
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: num % 1 === 0 ? 0 : 2,
  }).format(num);
}

export function formatDate(isoOrDate?: string | null): string {
  if (!isoOrDate) return "—";
  const parsed = new Date(isoOrDate);
  if (Number.isNaN(parsed.getTime())) return isoOrDate;
  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(parsed);
}

export function formatDateTime(iso?: string | null): string {
  if (!iso) return "—";
  const parsed = new Date(iso);
  if (Number.isNaN(parsed.getTime())) return iso;
  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(parsed);
}

export function extractErrorMessage(err: unknown, fallback = "Something went wrong."): string {
  if (!err || typeof err !== "object") return fallback;
  const anyErr = err as { response?: { data?: { message?: string; fieldErrors?: { field: string; message: string }[] } }; message?: string };
  const data = anyErr.response?.data;
  if (data?.fieldErrors && data.fieldErrors.length > 0) {
    return data.fieldErrors.map((f) => `${f.field}: ${f.message}`).join(" • ");
  }
  if (data?.message) return data.message;
  if (anyErr.message) return anyErr.message;
  return fallback;
}

export function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
