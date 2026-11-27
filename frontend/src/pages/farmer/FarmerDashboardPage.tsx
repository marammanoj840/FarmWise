import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import {
  AlertTriangle,
  Bell,
  CalendarCheck,
  CloudSun,
  IndianRupee,
  MapPin,
  Package,
  Sprout,
  TrendingUp,
} from "lucide-react";
import { farmerApi } from "@/api/services";
import { Badge, Card } from "@/components/ui/Card";
import { Breadcrumbs, Skeleton } from "@/components/ui/Feedback";
import { Button } from "@/components/ui/Button";
import { formatCurrencyINR, formatDate } from "@/utils/format";

export function FarmerDashboardPage() {
  const { data, isLoading } = useQuery({
    queryKey: ["farmer", "dashboard"],
    queryFn: farmerApi.dashboard,
  });

  if (isLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-10 w-64" />
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Skeleton className="h-28" />
          <Skeleton className="h-28" />
          <Skeleton className="h-28" />
          <Skeleton className="h-28" />
        </div>
        <Skeleton className="h-64" />
      </div>
    );
  }

  const weather = data?.primaryFarmWeather;

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Farmer Dashboard" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Farmer Operations Overview</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Live operational metrics from your farms, crops, inventory, and INR ledger.
          </p>
        </div>
        <div className="flex flex-wrap gap-2.5">
          <Link to="/app/farms">
            <Button size="sm" variant="outline" icon={<MapPin className="h-4 w-4" />}>
              Manage Farms
            </Button>
          </Link>
          <Link to="/app/cultivation">
            <Button size="sm" icon={<CalendarCheck className="h-4 w-4" />}>
              Log Activity
            </Button>
          </Link>
        </div>
      </div>

      {/* KPI Grid */}
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#4B6358] dark:text-[#9EB7AB]">
              Total Farms
            </span>
            <MapPin className="h-4 w-4 text-[#2A8C55]" />
          </div>
          <p className="mt-2 font-display text-3xl font-bold">{data?.totalFarms ?? 0}</p>
          <Link to="/app/farms" className="mt-2 inline-block text-xs font-semibold text-[#2A8C55] hover:underline">
            View farms →
          </Link>
        </Card>

        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#4B6358] dark:text-[#9EB7AB]">
              Active Crops
            </span>
            <Sprout className="h-4 w-4 text-[#2A8C55]" />
          </div>
          <p className="mt-2 font-display text-3xl font-bold">{data?.activeCrops ?? 0}</p>
          <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
            Est. Harvest (not guaranteed): {Math.round(data?.totalEstimatedYieldKg ?? 0).toLocaleString("en-IN")} kg
          </p>
        </Card>

        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#4B6358] dark:text-[#9EB7AB]">
              Pending Tasks & Stock
            </span>
            <Package className="h-4 w-4 text-amber-600" />
          </div>
          <p className="mt-2 font-display text-3xl font-bold">{data?.pendingTasksCount ?? 0}</p>
          <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
            {data?.lowStockInventoryCount ?? 0} low-stock inventory items
          </p>
        </Card>

        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#4B6358] dark:text-[#9EB7AB]">
              Recorded Net Balance
            </span>
            <IndianRupee className="h-4 w-4 text-[#2A8C55]" />
          </div>
          <p className="mt-2 font-display text-3xl font-bold">{formatCurrencyINR(data?.netBalanceInr)}</p>
          <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
            Income {formatCurrencyINR(data?.totalIncomeInr)} • Expenses {formatCurrencyINR(data?.totalExpensesInr)}
          </p>
        </Card>
      </div>

      {/* Weather + Upcoming Tasks */}
      <div className="grid gap-6 lg:grid-cols-12">
        <Card className="lg:col-span-5">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <CloudSun className="h-5 w-5 text-[#2A8C55]" />
              <h2 className="text-lg font-bold">Weather Summary</h2>
            </div>
            <Badge tone={weather?.liveDataAvailable ? "green" : "amber"}>
              {weather?.liveDataAvailable ? "Open-Meteo Live" : "Unavailable"}
            </Badge>
          </div>
          <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">{weather?.locationName}</p>

          {weather?.liveDataAvailable ? (
            <div className="mt-4 space-y-4">
              <div className="grid grid-cols-2 gap-3 rounded-xl bg-[#EEF3EE] p-4 dark:bg-[#152B22]">
                <div>
                  <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">Temperature</p>
                  <p className="text-2xl font-bold">{weather.temperatureC ?? "—"} °C</p>
                </div>
                <div>
                  <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">Humidity / Wind</p>
                  <p className="text-base font-bold">
                    {weather.humidityPct ?? "—"}% • {weather.windSpeedKmh ?? "—"} km/h
                  </p>
                </div>
              </div>
              <p className="text-sm font-medium">{weather.conditionSummary}</p>
              <ul className="space-y-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                {weather.recommendations.map((rec) => (
                  <li key={rec}>• {rec}</li>
                ))}
              </ul>
            </div>
          ) : (
            <div className="mt-4 rounded-xl border border-amber-200 bg-amber-50 p-4 text-xs text-amber-900 dark:border-amber-900/60 dark:bg-amber-950/30 dark:text-amber-200">
              <p className="font-semibold">Live weather unavailable</p>
              <p className="mt-1">{weather?.statusMessage}</p>
            </div>
          )}

          <Link to="/app/weather" className="mt-4 inline-block text-xs font-semibold text-[#2A8C55] hover:underline">
            Open full forecast →
          </Link>
        </Card>

        <Card className="lg:col-span-7">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-bold">Upcoming Cultivation Tasks</h2>
            <Link to="/app/cultivation" className="text-xs font-semibold text-[#2A8C55] hover:underline">
              Manage all →
            </Link>
          </div>
          {data?.upcomingTasks && data.upcomingTasks.length > 0 ? (
            <div className="mt-4 space-y-2.5">
              {data.upcomingTasks.map((t) => (
                <div
                  key={t.id}
                  className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-[#DCE5DC] p-3 dark:border-[#203D31]"
                >
                  <div>
                    <p className="text-sm font-semibold">{t.title}</p>
                    <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                      {t.stage} • Due {formatDate(t.dueDate)}
                    </p>
                  </div>
                  <Badge tone={t.priority === "HIGH" ? "red" : "amber"}>{t.priority}</Badge>
                </div>
              ))}
            </div>
          ) : (
            <p className="mt-4 text-sm text-[#4B6358] dark:text-[#9EB7AB]">No pending cultivation tasks scheduled.</p>
          )}
        </Card>
      </div>

      {/* Recent Expenses, Activities & Market Highlights */}
      <div className="grid gap-6 lg:grid-cols-3">
        <Card>
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-base font-bold">Recent Field Activities</h2>
            <Link to="/app/cultivation" className="text-xs text-[#2A8C55] hover:underline">
              View
            </Link>
          </div>
          <div className="space-y-2.5">
            {(data?.recentActivities || []).map((a) => (
              <div key={a.id} className="border-b border-[#E9EFE9] pb-2 last:border-0 dark:border-[#1D392E]">
                <p className="text-sm font-semibold">{a.title}</p>
                <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                  {a.stage} • {formatDate(a.activityDate)} • {formatCurrencyINR(a.costAmount)}
                </p>
              </div>
            ))}
            {(!data?.recentActivities || data.recentActivities.length === 0) && (
              <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">No activities logged yet.</p>
            )}
          </div>
        </Card>

        <Card>
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-base font-bold">Recent Expenses</h2>
            <Link to="/app/finance" className="text-xs text-[#2A8C55] hover:underline">
              Finance
            </Link>
          </div>
          <div className="space-y-2.5">
            {(data?.recentExpenses || []).map((e) => (
              <div key={e.id} className="flex items-center justify-between border-b border-[#E9EFE9] pb-2 last:border-0 dark:border-[#1D392E]">
                <div>
                  <p className="text-sm font-semibold">{e.title}</p>
                  <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                    {e.category} • {formatDate(e.expenseDate)}
                  </p>
                </div>
                <span className="text-sm font-bold text-red-700 dark:text-red-400">
                  {formatCurrencyINR(e.amountInr)}
                </span>
              </div>
            ))}
            {(!data?.recentExpenses || data.recentExpenses.length === 0) && (
              <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">No expenses recorded yet.</p>
            )}
          </div>
        </Card>

        <Card>
          <div className="mb-3 flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <TrendingUp className="h-4 w-4 text-[#2A8C55]" />
              <h2 className="text-base font-bold">Market Price Highlights</h2>
            </div>
            {data?.marketPricesAreSample && <Badge tone="amber">Sample Data</Badge>}
          </div>
          <div className="space-y-2.5">
            {(data?.marketPriceHighlights || []).map((p) => (
              <div key={p.id} className="flex items-center justify-between border-b border-[#E9EFE9] pb-2 last:border-0 dark:border-[#1D392E]">
                <div>
                  <p className="text-sm font-semibold">{p.commodity}</p>
                  <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                    {p.marketName}, {p.state}
                  </p>
                </div>
                <span className="text-sm font-bold text-[#10402E] dark:text-[#56CF88]">
                  {formatCurrencyINR(p.modalPriceInr)}/{p.unit.toLowerCase()}
                </span>
              </div>
            ))}
          </div>
          <Link to="/app/market-prices" className="mt-3 inline-block text-xs font-semibold text-[#2A8C55] hover:underline">
            Compare all mandis →
          </Link>
        </Card>
      </div>

      {/* Notices */}
      <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[#DCE5DC] bg-white/80 p-3.5 text-xs text-[#4B6358] dark:border-[#203D31] dark:bg-[#0F211A] dark:text-[#9EB7AB]">
        <span className="inline-flex items-center gap-2">
          <AlertTriangle className="h-4 w-4 text-amber-500" />
          {data?.financeDisclaimer}
        </span>
        <Link to="/app/notifications" className="inline-flex items-center gap-1 font-semibold text-[#10402E] dark:text-[#4FD085]">
          <Bell className="h-3.5 w-3.5" />
          {data?.unreadNotificationsCount ?? 0} unread notifications
        </Link>
      </div>
    </div>
  );
}
