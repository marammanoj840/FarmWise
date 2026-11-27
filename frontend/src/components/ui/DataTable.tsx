import { useMemo, useState, type ReactNode } from "react";
import { ArrowUpDown, Search } from "lucide-react";
import { EmptyState } from "@/components/ui/Feedback";

export interface ColumnDef<T> {
  key: string;
  header: string;
  sortable?: boolean;
  sortValue?: (row: T) => string | number;
  render: (row: T) => ReactNode;
  className?: string;
}

export function DataTable<T>({
  data,
  columns,
  rowKey,
  searchPlaceholder = "Search records...",
  searchableText,
  emptyTitle = "No records found",
  emptyDescription = "Try adjusting filters or adding a new entry.",
}: {
  data: T[];
  columns: ColumnDef<T>[];
  rowKey: (row: T) => string;
  searchPlaceholder?: string;
  searchableText?: (row: T) => string;
  emptyTitle?: string;
  emptyDescription?: string;
}) {
  const [query, setQuery] = useState("");
  const [sortKey, setSortKey] = useState<string | null>(null);
  const [sortAsc, setSortAsc] = useState(true);

  const processed = useMemo(() => {
    let list = data;
    if (query.trim() && searchableText) {
      const q = query.trim().toLowerCase();
      list = list.filter((row) => searchableText(row).toLowerCase().includes(q));
    }
    if (sortKey) {
      const col = columns.find((c) => c.key === sortKey);
      if (col?.sortValue) {
        const getter = col.sortValue;
        list = [...list].sort((a, b) => {
          const av = getter(a);
          const bv = getter(b);
          if (av < bv) return sortAsc ? -1 : 1;
          if (av > bv) return sortAsc ? 1 : -1;
          return 0;
        });
      }
    }
    return list;
  }, [data, query, searchableText, sortKey, sortAsc, columns]);

  const toggleSort = (key: string) => {
    if (sortKey === key) {
      setSortAsc((a) => !a);
    } else {
      setSortKey(key);
      setSortAsc(true);
    }
  };

  return (
    <div className="space-y-3">
      {searchableText && (
        <div className="relative max-w-sm">
          <Search className="pointer-events-none absolute left-3.5 top-3.5 h-4 w-4 text-[#6B8277]" />
          <input
            type="search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder={searchPlaceholder}
            className="min-h-11 w-full rounded-xl border border-[#C9D6CA] bg-white pl-10 pr-4 text-sm focus:border-[#2A8C55] focus:outline-none dark:border-[#244337] dark:bg-[#0D1E17]"
          />
        </div>
      )}

      {processed.length === 0 ? (
        <EmptyState title={emptyTitle} description={emptyDescription} />
      ) : (
        <div className="overflow-x-auto rounded-xl border border-[#DCE5DC] dark:border-[#203D31]">
          <table className="min-w-full divide-y divide-[#DCE5DC] text-left text-sm dark:divide-[#203D31]">
            <thead className="bg-[#EEF3EE] text-xs font-semibold uppercase tracking-wider text-[#334F43] dark:bg-[#132920] dark:text-[#9EB7AB]">
              <tr>
                {columns.map((col) => (
                  <th key={col.key} className={`px-4 py-3 ${col.className || ""}`}>
                    {col.sortable && col.sortValue ? (
                      <button
                        type="button"
                        onClick={() => toggleSort(col.key)}
                        className="inline-flex items-center gap-1 hover:text-[#10402E] dark:hover:text-white"
                      >
                        {col.header}
                        <ArrowUpDown className="h-3.5 w-3.5" />
                      </button>
                    ) : (
                      col.header
                    )}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E9EFE9] bg-white dark:divide-[#1A352A] dark:bg-[#0F211A]">
              {processed.map((row) => (
                <tr key={rowKey(row)} className="hover:bg-[#F7FAF7] dark:hover:bg-[#132920]">
                  {columns.map((col) => (
                    <td key={col.key} className={`px-4 py-3.5 align-middle ${col.className || ""}`}>
                      {col.render(row)}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
