import { Link, useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft, CloudSun, MapPin, Sprout } from "lucide-react";
import { cropApi, farmApi, weatherApi } from "@/api/services";
import { Badge, Card } from "@/components/ui/Card";
import { Breadcrumbs, Skeleton } from "@/components/ui/Feedback";
import { Button } from "@/components/ui/Button";
import { formatDate } from "@/utils/format";

export function FarmDetailPage() {
  const { id } = useParams<{ id: string }>();

  const { data: farm, isLoading } = useQuery({
    queryKey: ["farm", id],
    queryFn: () => farmApi.get(id!),
    enabled: Boolean(id),
  });

  const { data: crops } = useQuery({
    queryKey: ["crops", "farm", id],
    queryFn: () => cropApi.list(id, 0, 50),
    enabled: Boolean(id),
  });

  const { data: weather } = useQuery({
    queryKey: ["weather", "farm", id],
    queryFn: () => weatherApi.forFarm(id!),
    enabled: Boolean(id),
  });

  if (isLoading) {
    return <Skeleton className="h-64" />;
  }

  if (!farm) {
    return (
      <Card>
        <p>Farm not found.</p>
        <Link to="/app/farms" className="mt-3 inline-block text-sm text-[#2A8C55]">
          Back to farms
        </Link>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      <Breadcrumbs
        items={[
          { label: "Dashboard", to: "/app/farmer" },
          { label: "My Farms", to: "/app/farms" },
          { label: farm.name },
        ]}
      />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">{farm.name}</h1>
          <p className="mt-1 inline-flex items-center gap-1.5 text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            <MapPin className="h-4 w-4" />
            {farm.location?.villageOrCity}, {farm.location?.district}, {farm.location?.state}
          </p>
        </div>
        <Link to="/app/farms">
          <Button variant="outline" icon={<ArrowLeft className="h-4 w-4" />}>
            All Farms
          </Button>
        </Link>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        <Card className="space-y-2">
          <p className="text-xs font-semibold uppercase text-[#4B6358] dark:text-[#9EB7AB]">Land & Soil</p>
          <p className="text-2xl font-bold">
            {farm.landArea} {farm.areaUnit}
          </p>
          <div className="flex flex-wrap gap-2 pt-1">
            <Badge tone="green">{farm.soilType}</Badge>
            <Badge tone="blue">{farm.irrigationType}</Badge>
          </div>
          {farm.description && <p className="pt-2 text-xs text-[#4B6358] dark:text-[#9EB7AB]">{farm.description}</p>}
        </Card>

        <Card className="space-y-2 md:col-span-2">
          <div className="flex items-center justify-between">
            <span className="inline-flex items-center gap-2 font-bold">
              <CloudSun className="h-5 w-5 text-[#2A8C55]" />
              Farm Weather & Advisories
            </span>
            <Badge tone={weather?.liveDataAvailable ? "green" : "amber"}>
              {weather?.liveDataAvailable ? "Live Open-Meteo" : "Unavailable"}
            </Badge>
          </div>
          {weather?.liveDataAvailable ? (
            <div className="grid gap-3 pt-2 sm:grid-cols-4">
              <div className="rounded-xl bg-[#EEF3EE] p-3 dark:bg-[#152B22]">
                <p className="text-xs text-[#4B6358]">Temperature</p>
                <p className="text-xl font-bold">{weather.temperatureC} °C</p>
              </div>
              <div className="rounded-xl bg-[#EEF3EE] p-3 dark:bg-[#152B22]">
                <p className="text-xs text-[#4B6358]">Humidity</p>
                <p className="text-xl font-bold">{weather.humidityPct}%</p>
              </div>
              <div className="rounded-xl bg-[#EEF3EE] p-3 dark:bg-[#152B22]">
                <p className="text-xs text-[#4B6358]">Rainfall</p>
                <p className="text-xl font-bold">{weather.rainfallMm ?? 0} mm</p>
              </div>
              <div className="rounded-xl bg-[#EEF3EE] p-3 dark:bg-[#152B22]">
                <p className="text-xs text-[#4B6358]">Wind</p>
                <p className="text-xl font-bold">{weather.windSpeedKmh} km/h</p>
              </div>
            </div>
          ) : (
            <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">{weather?.statusMessage}</p>
          )}
        </Card>
      </div>

      <Card>
        <div className="mb-4 flex items-center justify-between">
          <h2 className="inline-flex items-center gap-2 text-lg font-bold">
            <Sprout className="h-5 w-5 text-[#2A8C55]" />
            Crops on This Farm
          </h2>
          <Link to="/app/crops">
            <Button size="sm" variant="outline">
              Manage Crops
            </Button>
          </Link>
        </div>
        {crops && crops.items.length > 0 ? (
          <div className="grid gap-3 sm:grid-cols-2">
            {crops.items.map((c) => (
              <div key={c.id} className="rounded-xl border border-[#DCE5DC] p-4 dark:border-[#203D31]">
                <div className="flex items-center justify-between">
                  <p className="font-bold">
                    {c.cropName} {c.varietyName ? `(${c.varietyName})` : ""}
                  </p>
                  <Badge tone="green">{c.cultivationStatus}</Badge>
                </div>
                <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                  Stage: {c.growthStage} • Area: {c.plantedArea} {c.areaUnit} • Sown: {formatDate(c.sowingDate)}
                </p>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">No crops assigned to this farm yet.</p>
        )}
      </Card>
    </div>
  );
}
