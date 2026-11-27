import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { CloudOff, CloudSun, Droplets, Search, Wind } from "lucide-react";
import { farmApi, weatherApi } from "@/api/services";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Select } from "@/components/ui/Field";
import { Breadcrumbs, Skeleton } from "@/components/ui/Feedback";

export function WeatherPage() {
  const [selectedFarmId, setSelectedFarmId] = useState("");
  const [locationQuery, setLocationQuery] = useState("Ludhiana, Punjab");
  const [activeLookup, setActiveLookup] = useState("Ludhiana, Punjab");

  const { data: farms } = useQuery({ queryKey: ["farms", "all"], queryFn: () => farmApi.list(undefined, 0, 50) });

  const { data: weather, isLoading, refetch } = useQuery({
    queryKey: ["weather", selectedFarmId, activeLookup],
    queryFn: () => (selectedFarmId ? weatherApi.forFarm(selectedFarmId) : weatherApi.lookup(activeLookup)),
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Dashboard", to: "/app/farmer" }, { label: "Weather & Environment" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Weather & Agricultural Advisories</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Powered by Open-Meteo integration interface. Unavailable feeds are reported transparently without simulated numbers.
          </p>
        </div>
        <Button variant="outline" onClick={() => refetch()}>
          Refresh Forecast
        </Button>
      </div>

      <Card className="grid gap-4 md:grid-cols-12 md:items-end">
        <div className="md:col-span-5">
          <Select
            label="Select Owned Farm"
            value={selectedFarmId}
            onChange={(e) => setSelectedFarmId(e.target.value)}
          >
            <option value="">Custom Location Lookup</option>
            {(farms?.items || []).map((f) => (
              <option key={f.id} value={f.id}>
                {f.name} ({f.location?.district}, {f.location?.state})
              </option>
            ))}
          </Select>
        </div>

        {!selectedFarmId && (
          <>
            <div className="md:col-span-5">
              <label className="mb-1.5 block text-sm font-medium">City / District Name</label>
              <input
                type="text"
                value={locationQuery}
                onChange={(e) => setLocationQuery(e.target.value)}
                className="min-h-11 w-full rounded-xl border border-[#C9D6CA] bg-white px-3.5 text-sm dark:border-[#244337] dark:bg-[#0D1E17]"
              />
            </div>
            <div className="md:col-span-2">
              <Button className="w-full" onClick={() => setActiveLookup(locationQuery)} icon={<Search className="h-4 w-4" />}>
                Lookup
              </Button>
            </div>
          </>
        )}
      </Card>

      {isLoading ? (
        <Skeleton className="h-72" />
      ) : weather?.liveDataAvailable ? (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Card>
              <div className="flex items-center justify-between text-xs text-[#4B6358]">
                <span>Temperature</span>
                <CloudSun className="h-4 w-4 text-amber-500" />
              </div>
              <p className="mt-2 font-display text-3xl font-bold">{weather.temperatureC} °C</p>
              <p className="mt-1 text-xs">{weather.conditionSummary}</p>
            </Card>
            <Card>
              <div className="flex items-center justify-between text-xs text-[#4B6358]">
                <span>Humidity</span>
                <Droplets className="h-4 w-4 text-sky-500" />
              </div>
              <p className="mt-2 font-display text-3xl font-bold">{weather.humidityPct}%</p>
              <p className="mt-1 text-xs">{weather.locationName}</p>
            </Card>
            <Card>
              <div className="flex items-center justify-between text-xs text-[#4B6358]">
                <span>Rainfall</span>
                <Droplets className="h-4 w-4 text-emerald-600" />
              </div>
              <p className="mt-2 font-display text-3xl font-bold">{weather.rainfallMm ?? 0} mm</p>
              <p className="mt-1 text-xs">Provider: {weather.providerName}</p>
            </Card>
            <Card>
              <div className="flex items-center justify-between text-xs text-[#4B6358]">
                <span>Wind Speed</span>
                <Wind className="h-4 w-4 text-teal-600" />
              </div>
              <p className="mt-2 font-display text-3xl font-bold">{weather.windSpeedKmh} km/h</p>
              <Badge tone={weather.alertLevel === "WARNING" ? "red" : "green"} className="mt-1">
                Alert: {weather.alertLevel}
              </Badge>
            </Card>
          </div>

          <div className="grid gap-6 lg:grid-cols-12">
            <Card className="lg:col-span-7">
              <h2 className="mb-4 text-lg font-bold">5-Day Temperature Forecast (°C)</h2>
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={weather.dailyForecast}>
                    <CartesianGrid strokeDasharray="3 3" opacity={0.25} />
                    <XAxis dataKey="date" fontSize={12} />
                    <YAxis fontSize={12} />
                    <Tooltip />
                    <Area type="monotone" dataKey="tempMaxC" name="Max °C" stroke="#D97706" fill="#FDE68A" />
                    <Area type="monotone" dataKey="tempMinC" name="Min °C" stroke="#2A8C55" fill="#A7F3D0" />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </Card>

            <Card className="space-y-3 lg:col-span-5">
              <h2 className="text-lg font-bold">Field Weather Advisories</h2>
              <ul className="space-y-2 text-sm text-[#355245] dark:text-[#CBE0D4]">
                {weather.recommendations.map((rec) => (
                  <li key={rec} className="rounded-xl bg-[#EEF3EE] p-3 dark:bg-[#152B22]">
                    {rec}
                  </li>
                ))}
              </ul>
            </Card>
          </div>
        </>
      ) : (
        <Card className="border-amber-300 bg-amber-50/70 p-6 dark:border-amber-900 dark:bg-amber-950/30">
          <div className="flex items-start gap-3">
            <CloudOff className="mt-0.5 h-6 w-6 text-amber-600" />
            <div>
              <h2 className="text-lg font-bold">Live Weather Information Unavailable</h2>
              <p className="mt-1 text-sm text-[#4B6358] dark:text-[#9EB7AB]">
                {weather?.statusMessage || "Could not connect to Open-Meteo service."}
              </p>
            </div>
          </div>
        </Card>
      )}
    </div>
  );
}
