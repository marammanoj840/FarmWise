import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { BookOpen, Droplets, Search, Sprout } from "lucide-react";
import { cropApi, soilApi } from "@/api/services";
import { Badge, Card } from "@/components/ui/Card";
import { Skeleton } from "@/components/ui/Feedback";

export function ResourcesPage() {
  const [search, setSearch] = useState("");

  const { data: knowledge, isLoading: kbLoading } = useQuery({
    queryKey: ["public", "knowledge", search],
    queryFn: () => soilApi.knowledgeBase(search || undefined),
  });

  const { data: catalog } = useQuery({
    queryKey: ["public", "crop-catalog"],
    queryFn: cropApi.catalog,
  });

  return (
    <div className="mx-auto max-w-7xl space-y-10 px-4 py-14 sm:px-6 lg:px-8">
      <div className="max-w-3xl space-y-3">
        <Badge tone="green">Farmer Resources & Crop Library</Badge>
        <h1 className="font-display text-4xl font-bold sm:text-5xl">Agronomic Guides & Seasonal Reference</h1>
        <p className="text-base text-[#4B6358] dark:text-[#9EB7AB]">
          Browse practical Indian crop cultivation guides, ideal soil pH windows, irrigation schedules, and pest scouting
          checklists sourced from our backend knowledge base.
        </p>
      </div>

      <div className="relative max-w-md">
        <Search className="pointer-events-none absolute left-3.5 top-3.5 h-4 w-4 text-[#6B8277]" />
        <input
          type="search"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search crop or soil type (e.g., Wheat, Loam, Cotton)..."
          className="min-h-11 w-full rounded-xl border border-[#C9D6CA] bg-white pl-10 pr-4 text-sm focus:border-[#2A8C55] focus:outline-none dark:border-[#244337] dark:bg-[#0D1E17]"
        />
      </div>

      {kbLoading ? (
        <div className="grid gap-5 md:grid-cols-2">
          <Skeleton className="h-52" />
          <Skeleton className="h-52" />
        </div>
      ) : (
        <div className="grid gap-6 md:grid-cols-2">
          {(knowledge || []).map((item) => (
            <Card key={item.id} className="space-y-3">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h2 className="text-xl font-bold">{item.cropName}</h2>
                  <p className="text-xs font-medium text-[#2A8C55] dark:text-[#4FD085]">
                    {item.category} • {item.sowingSeason}
                  </p>
                </div>
                <Badge tone="amber">pH {item.idealPhRange}</Badge>
              </div>
              <div className="space-y-2 text-sm text-[#355245] dark:text-[#B9CFC3]">
                <p><strong>Suitable Soils:</strong> {item.suitableSoilTypes}</p>
                <p><strong>Irrigation:</strong> {item.irrigationSchedule}</p>
                <p><strong>Nutrients:</strong> {item.nutrientGuidance}</p>
                <p><strong>Pests & Diseases to Watch:</strong> {item.commonPestsDiseases}</p>
              </div>
              <p className="rounded-xl bg-[#EEF3EE] p-2.5 text-xs text-[#4B6358] dark:bg-[#152B22] dark:text-[#9EB7AB]">
                {item.advisoryDisclaimer}
              </p>
            </Card>
          ))}
        </div>
      )}

      {catalog && catalog.length > 0 && (
        <div className="space-y-4 border-t border-[#DCE5DC] pt-8 dark:border-[#203D31]">
          <div className="flex items-center gap-2">
            <BookOpen className="h-5 w-5 text-[#10402E] dark:text-[#4FD085]" />
            <h2 className="font-display text-2xl font-bold">Supported Crop Varieties Reference</h2>
          </div>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {catalog.map((c) => (
              <Card key={c.id} padding="p-4">
                <div className="flex items-center justify-between">
                  <h3 className="font-bold">{c.name}</h3>
                  <Badge tone="slate">{c.category}</Badge>
                </div>
                <p className="mt-1 text-xs italic text-[#4B6358] dark:text-[#9EB7AB]">{c.scientificName}</p>
                <div className="mt-3 flex flex-wrap gap-3 text-xs">
                  <span className="inline-flex items-center gap-1">
                    <Sprout className="h-3.5 w-3.5 text-[#2A8C55]" />
                    {c.typicalDurationDays ?? "—"} days
                  </span>
                  <span className="inline-flex items-center gap-1">
                    <Droplets className="h-3.5 w-3.5 text-sky-600" />
                    Water: {c.waterRequirement ?? "Moderate"}
                  </span>
                </div>
                {c.varieties.length > 0 && (
                  <p className="mt-2.5 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                    <strong>Varieties:</strong> {c.varieties.map((v) => v.name).join(", ")}
                  </p>
                )}
              </Card>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
