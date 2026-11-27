import { forwardRef, type InputHTMLAttributes, type SelectHTMLAttributes, type TextareaHTMLAttributes } from "react";

interface FieldWrapperProps {
  label?: string;
  error?: string;
  hint?: string;
  id?: string;
  required?: boolean;
}

const baseFieldClass =
  "w-full min-h-11 rounded-xl border border-[#C9D6CA] bg-white px-3.5 py-2.5 text-sm text-[#11211B] placeholder:text-[#6B8277] transition focus:border-[#2A8C55] focus:outline-none focus:ring-2 focus:ring-[#2A8C55]/25 disabled:bg-slate-100 dark:border-[#244337] dark:bg-[#0D1E17] dark:text-[#EEF4F0] dark:placeholder:text-[#789286] dark:focus:border-[#4FD085]";

export const Input = forwardRef<
  HTMLInputElement,
  InputHTMLAttributes<HTMLInputElement> & FieldWrapperProps
>(({ label, error, hint, id, required, className = "", ...props }, ref) => {
  const inputId = id || props.name;
  return (
    <div className="space-y-1.5">
      {label && (
        <label htmlFor={inputId} className="block text-sm font-medium text-[#1D352B] dark:text-[#CFE0D6]">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      <input
        ref={ref}
        id={inputId}
        className={`${baseFieldClass} ${error ? "border-red-500 focus:border-red-500 focus:ring-red-500/25" : ""} ${className}`}
        {...props}
      />
      {error && <p className="text-xs font-medium text-red-600 dark:text-red-400">{error}</p>}
      {!error && hint && <p className="text-xs text-[#597065] dark:text-[#8CA59A]">{hint}</p>}
    </div>
  );
});
Input.displayName = "Input";

export const Select = forwardRef<
  HTMLSelectElement,
  SelectHTMLAttributes<HTMLSelectElement> & FieldWrapperProps
>(({ label, error, hint, id, required, children, className = "", ...props }, ref) => {
  const selectId = id || props.name;
  return (
    <div className="space-y-1.5">
      {label && (
        <label htmlFor={selectId} className="block text-sm font-medium text-[#1D352B] dark:text-[#CFE0D6]">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      <select
        ref={ref}
        id={selectId}
        className={`${baseFieldClass} ${error ? "border-red-500" : ""} ${className}`}
        {...props}
      >
        {children}
      </select>
      {error && <p className="text-xs font-medium text-red-600 dark:text-red-400">{error}</p>}
      {!error && hint && <p className="text-xs text-[#597065] dark:text-[#8CA59A]">{hint}</p>}
    </div>
  );
});
Select.displayName = "Select";

export const Textarea = forwardRef<
  HTMLTextAreaElement,
  TextareaHTMLAttributes<HTMLTextAreaElement> & FieldWrapperProps
>(({ label, error, hint, id, required, className = "", rows = 3, ...props }, ref) => {
  const fieldId = id || props.name;
  return (
    <div className="space-y-1.5">
      {label && (
        <label htmlFor={fieldId} className="block text-sm font-medium text-[#1D352B] dark:text-[#CFE0D6]">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      <textarea
        ref={ref}
        id={fieldId}
        rows={rows}
        className={`${baseFieldClass} min-h-24 ${error ? "border-red-500" : ""} ${className}`}
        {...props}
      />
      {error && <p className="text-xs font-medium text-red-600 dark:text-red-400">{error}</p>}
      {!error && hint && <p className="text-xs text-[#597065] dark:text-[#8CA59A]">{hint}</p>}
    </div>
  );
});
Textarea.displayName = "Textarea";
