import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { Info, Search, TrendingUp } from "lucide-react";
import { marketPriceApi } from "@/api/services";
import { formatCurrencyINR, formatDate, formatDateTime } from "@/utils/format";
import { Badge, Card } from "@/components/ui/Card";
import { Input, Select } from "@/components/ui/Field";
import { Breadcrumbs, Skeleton } from "@/components/ui/Feedback";

export function MarketPricesPage() {
  const [commodity, setCommodity] = useState("");
  const [stateFilter, setStateFilter] = useState("");
  const [marketFilter, setMarketFilter] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["market-prices", commodity, stateFilter, marketFilter],
    queryFn: () =>
      marketPriceApi.search({
        commodity: commodity || undefined,
        state: stateFilter || undefined,
        market: marketFilter || undefined,
      }),
  });

  const chartData = (data?.prices || []).slice(0, 10).map((p) => ({
    label: `${p.commodity.split(" ")[0]} (${p.marketName.split(" ")[0]})`,
    Min: Number(p.minPriceInr),
    Modal: Number(p.modalPriceInr),
    Max: Number(p.maxPriceInr),
  }));

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Market Price Intelligence" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Mandi Market Price Intelligence</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Compare commodity min, modal, and max prices across Indian mandis (INR per Quintal).
          </p>
        </div>
        <Badge tone={data?.liveFeedConfigured ? "green" : "amber"}>
          {data?.liveFeedConfigured ? `Live: ${data.providerName}` : "Sample Reference Data"}
        </Badge>
      </div>

      {data && (
        <div className="flex items-start gap-2.5 rounded-xl border border-amber-200 bg-amber-50/75 p-3.5 text-xs text-amber-950 dark:border-amber-900/60 dark:bg-amber-950/30 dark:text-amber-200">
          <Info className="mt-0.5 h-4 w-4 shrink-0" />
          <span>{data.notice}</span>
        </div>
      )}

      <Card className="grid gap-4 sm:grid-cols-3">
        <div className="relative">
          <Select label="Commodity" value={commodity} onChange={(e) => setCommodity(e.target.value)}>
            <option value="">All Commodities</option>
            {(data?.commodities || []).map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </Select>
        </div>
        <Input
          label="State Filter"
          placeholder="e.g., Punjab, Maharashtra..."
          value={stateFilter}
          onChange={(e) => setStateFilter(e.target.value)}
        />
        <div className="relative">
          <Input
            label="Mandi / Market Search"
            placeholder="e.g., Khanna, Nashik..."
            value={marketFilter}
            onChange={(e) => setMarketFilter(e.target.value)}
          />
          <Search className="pointer-events-none absolute right-3.5 top-10 h-4 w-4 text-[#6B8277]" />
        </div>
      </Card>

      {isLoading ? (
        <Skeleton className="h-72" />
      ) : (
        <>
          {chartData.length > 0 && (
            <Card>
              <div className="mb-4 flex items-center gap-2">
                <TrendingUp className="h-5 w-5 text-[#2A8C55]" />
                <h2 className="text-lg font-bold">Price Comparison Across Mandis (INR ₹)</h2>
              </div>
              <div className="h-72 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" opacity={0.25} />
                    <XAxis dataKey="label" fontSize={11} />
                    <YAxis fontSize={12} />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="Min" fill="#94A3B8" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="Modal" fill="#2A8C55" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="Max" fill="#D97706" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </Card>
          )}

          <Card padding="p-0" className="overflow-x-auto">
            <table className="min-w-full divide-y divide-[#DCE5DC] text-left text-sm dark:divide-[#203D31]">
              <thead className="bg-[#EEF3EE] text-xs font-semibold uppercase text-[#355245] dark:bg-[#132920] dark:text-[#9EB7AB]">
                <tr>
                  <th className="px-4 py-3">Commodity</th>
                  <th className="px-4 py-3">Market / Mandi</th>
                  <th className="px-4 py-3">State</th>
                  <th className="px-4 py-3">Min / Max (₹)</th>
                  <th className="px-4 py-3">Modal Price (₹)</th>
                  <th className="px-4 py-3">Date & Source</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#E9EFE9] dark:divide-[#1D392E]">
                {(data?.prices || []).map((p) => (
                  <tr key={p.id}>
                    <td className="px-4 py-3">
                      <p className="font-bold">{p.commodity}</p>
                      <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">{p.variety}</p>
                    </td>
                    <td className="px-4 py-3 text-xs">
                      <p className="font-semibold">{p.marketName}</p>
                      <p className="text-[#4B6358] dark:text-[#9EB7AB]">{p.district}</p>
                    </td>
                    <td className="px-4 py-3 text-xs">{p.state}</td>
                    <td className="px-4 py-3 text-xs">
                      {formatCurrencyINR(p.minPriceInr)} – {formatCurrencyINR(p.maxPriceInr)}
                    </td>
                    <td className="px-4 py-3 font-bold text-[#10402E] dark:text-[#56CF88]">
                      {formatCurrencyINR(p.modalPriceInr)} / {p.unit.toLowerCase()}
                    </td>
                    <td className="px-4 py-3 text-xs">
                      <p>{formatDate(p.priceDate)}</p>
                      <div className="mt-1 flex items-center gap-1.5">
                        {p.sampleData && <Badge tone="amber">SAMPLE</Badge>}
                        <span className="text-[11px] text-[#4B6358]">Updated {formatDateTime(p.fetchedAt)}</span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </Card>
        </>
      )}
    </div>
  );
}
