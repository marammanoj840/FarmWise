import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Edit, Plus, Sprout, Trash2 } from "lucide-react";
import { cropApi, farmApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage, formatDate } from "@/utils/format";
import type { FarmCrop } from "@/types/api";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { ConfirmDialog, Modal } from "@/components/ui/Modal";
import { Breadcrumbs, EmptyState, Skeleton } from "@/components/ui/Feedback";

const cropSchema = z.object({
  farmId: z.string().min(1, "Select a farm"),
  cropName: z.string().min(2, "Crop name is required"),
  varietyName: z.string().optional(),
  season: z.string().min(2),
  plantedArea: z.coerce.number().positive(),
  areaUnit: z.string().min(1),
  sowingDate: z.string().optional(),
  expectedHarvestDate: z.string().optional(),
  actualHarvestDate: z.string().optional(),
  growthStage: z.string().min(1),
  cultivationStatus: z.string().min(1),
  expectedYieldKg: z.string().optional(),
  actualYieldKg: z.string().optional(),
  fieldObservations: z.string().optional(),
});

type CropFormValues = z.infer<typeof cropSchema>;

export function CropsPage() {
  const queryClient = useQueryClient();
  const { success, error } = useToast();
  const [selectedFarmFilter, setSelectedFarmFilter] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<FarmCrop | null>(null);
  const [deleting, setDeleting] = useState<FarmCrop | null>(null);

  const { data: farms } = useQuery({ queryKey: ["farms", "all"], queryFn: () => farmApi.list(undefined, 0, 50) });
  const { data: catalog } = useQuery({ queryKey: ["crops", "catalog"], queryFn: cropApi.catalog });
  const { data: crops, isLoading } = useQuery({
    queryKey: ["crops", selectedFarmFilter],
    queryFn: () => cropApi.list(selectedFarmFilter || undefined, 0, 50),
  });

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<CropFormValues>({
    resolver: zodResolver(cropSchema) as never,
    defaultValues: {
      season: "Rabi",
      areaUnit: "ACRES",
      growthStage: "SOWING",
      cultivationStatus: "GROWING",
    },
  });

  const watchedCropName = watch("cropName");
  const matchedCatalog = catalog?.find((c) => c.name.toLowerCase() === (watchedCropName || "").toLowerCase());

  const openCreate = () => {
    setEditing(null);
    reset({
      farmId: farms?.items[0]?.id || "",
      cropName: catalog?.[0]?.name || "Wheat",
      varietyName: "",
      season: "Rabi",
      plantedArea: 2.0,
      areaUnit: farms?.items[0]?.areaUnit || "ACRES",
      sowingDate: "",
      expectedHarvestDate: "",
      actualHarvestDate: "",
      growthStage: "SOWING",
      cultivationStatus: "GROWING",
      expectedYieldKg: "",
      actualYieldKg: "",
      fieldObservations: "",
    });
    setModalOpen(true);
  };

  const openEdit = (fc: FarmCrop) => {
    setEditing(fc);
    reset({
      farmId: fc.farmId,
      cropName: fc.cropName,
      varietyName: fc.varietyName || "",
      season: fc.season,
      plantedArea: fc.plantedArea,
      areaUnit: fc.areaUnit,
      sowingDate: fc.sowingDate || "",
      expectedHarvestDate: fc.expectedHarvestDate || "",
      actualHarvestDate: fc.actualHarvestDate || "",
      growthStage: fc.growthStage,
      cultivationStatus: fc.cultivationStatus,
      expectedYieldKg: fc.expectedYieldKg != null ? String(fc.expectedYieldKg) : "",
      actualYieldKg: fc.actualYieldKg != null ? String(fc.actualYieldKg) : "",
      fieldObservations: fc.fieldObservations || "",
    });
    setModalOpen(true);
  };

  const saveMutation = useMutation({
    mutationFn: (v: CropFormValues) => {
      const payload = {
        ...v,
        cropId: matchedCatalog?.id || null,
        sowingDate: v.sowingDate || null,
        expectedHarvestDate: v.expectedHarvestDate || null,
        actualHarvestDate: v.actualHarvestDate || null,
        expectedYieldKg: v.expectedYieldKg ? Number(v.expectedYieldKg) : null,
        actualYieldKg: v.actualYieldKg ? Number(v.actualYieldKg) : null,
      };
      return editing ? cropApi.update(editing.id, payload) : cropApi.create(payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["crops"] });
      queryClient.invalidateQueries({ queryKey: ["farmer", "dashboard"] });
      setModalOpen(false);
      success(editing ? "Crop updated" : "Crop planted");
    },
    onError: (err) => error("Failed to save crop", extractErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => cropApi.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["crops"] });
      setDeleting(null);
      success("Crop record deleted");
    },
    onError: (err) => error("Delete failed", extractErrorMessage(err)),
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Dashboard", to: "/app/farmer" }, { label: "Crops & Harvests" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Crop Management</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Track varieties, sowing dates, growth stages, field observations, and harvest yields.
          </p>
        </div>
        <Button onClick={openCreate} disabled={!farms?.items.length} icon={<Plus className="h-4 w-4" />}>
          Add Crop Planting
        </Button>
      </div>

      <div className="max-w-xs">
        <Select
          label="Filter by Farm"
          value={selectedFarmFilter}
          onChange={(e) => setSelectedFarmFilter(e.target.value)}
        >
          <option value="">All Farms</option>
          {(farms?.items || []).map((f) => (
            <option key={f.id} value={f.id}>
              {f.name}
            </option>
          ))}
        </Select>
      </div>

      {isLoading ? (
        <Skeleton className="h-48" />
      ) : !crops || crops.items.length === 0 ? (
        <EmptyState
          icon={<Sprout className="h-6 w-6" />}
          title="No crop plantings recorded"
          description={
            farms?.items.length
              ? "Add your first crop cycle to monitor growth stages and expected harvests."
              : "Create a farm first before adding crops."
          }
        />
      ) : (
        <div className="grid gap-5 md:grid-cols-2">
          {crops.items.map((c) => (
            <Card key={c.id} className="space-y-3">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h2 className="text-lg font-bold">
                    {c.cropName} {c.varietyName ? `• ${c.varietyName}` : ""}
                  </h2>
                  <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                    {c.farmName} • {c.season} • {c.plantedArea} {c.areaUnit}
                  </p>
                </div>
                <div className="flex gap-1.5">
                  <Badge tone={c.cultivationStatus === "HARVESTED" ? "amber" : "green"}>{c.cultivationStatus}</Badge>
                  <Badge tone="blue">{c.growthStage}</Badge>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-2 rounded-xl bg-[#EEF3EE] p-3 text-xs dark:bg-[#152B22]">
                <div>
                  <span className="text-[#4B6358] dark:text-[#9EB7AB]">Sown</span>
                  <p className="font-semibold">{formatDate(c.sowingDate)}</p>
                </div>
                <div>
                  <span className="text-[#4B6358] dark:text-[#9EB7AB]">Est. Harvest</span>
                  <p className="font-semibold">{formatDate(c.expectedHarvestDate)}</p>
                </div>
                <div>
                  <span className="text-[#4B6358] dark:text-[#9EB7AB]">Yield (Est/Actual)</span>
                  <p className="font-semibold">
                    {c.expectedYieldKg ?? "—"} / {c.actualYieldKg ?? "—"} kg
                  </p>
                </div>
              </div>

              {c.fieldObservations && (
                <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">{c.fieldObservations}</p>
              )}

              <div className="flex justify-end gap-2 border-t border-[#E9EFE9] pt-2.5 dark:border-[#1D392E]">
                <Button size="sm" variant="ghost" onClick={() => openEdit(c)} icon={<Edit className="h-3.5 w-3.5" />}>
                  Update Stage / Harvest
                </Button>
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => setDeleting(c)}
                  className="text-red-600"
                  icon={<Trash2 className="h-3.5 w-3.5" />}
                />
              </div>
            </Card>
          ))}
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? "Update Crop Cycle" : "Add Crop"}>
        <form onSubmit={handleSubmit((v) => saveMutation.mutate(v))} className="space-y-4">
          <Select label="Farm" required {...register("farmId")} error={errors.farmId?.message}>
            <option value="">Select farm...</option>
            {(farms?.items || []).map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </Select>

          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <Input
                label="Crop Name"
                list="crop-catalog-list"
                required
                {...register("cropName")}
                error={errors.cropName?.message}
              />
              <datalist id="crop-catalog-list">
                {(catalog || []).map((c) => (
                  <option key={c.id} value={c.name} />
                ))}
              </datalist>
            </div>
            <div>
              <Input label="Variety" list="variety-list" {...register("varietyName")} />
              {matchedCatalog && matchedCatalog.varieties.length > 0 && (
                <datalist id="variety-list">
                  {matchedCatalog.varieties.map((v) => (
                    <option
                      key={v.id}
                      value={v.name}
                      onClick={() => setValue("varietyName", v.name)}
                    />
                  ))}
                </datalist>
              )}
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-3">
            <Select label="Season" {...register("season")}>
              <option value="Kharif">Kharif</option>
              <option value="Rabi">Rabi</option>
              <option value="Zaid">Zaid</option>
              <option value="Annual">Annual</option>
            </Select>
            <Input label="Planted Area" type="number" step="0.01" required {...register("plantedArea")} />
            <Select label="Unit" {...register("areaUnit")}>
              <option value="ACRES">Acres</option>
              <option value="HECTARES">Hectares</option>
              <option value="BIGHA">Bigha</option>
            </Select>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <Select label="Growth Stage" {...register("growthStage")}>
              <option value="PLANNING">Planning</option>
              <option value="SOWING">Sowing</option>
              <option value="GERMINATION">Germination</option>
              <option value="VEGETATIVE">Vegetative</option>
              <option value="CROWN_ROOT_INITIATION">Crown Root Initiation</option>
              <option value="FLOWERING_FRUIT_SET">Flowering / Fruit Set</option>
              <option value="GRAIN_FILLING">Grain / Pod Filling</option>
              <option value="MATURITY">Maturity</option>
              <option value="HARVESTED">Harvested</option>
            </Select>
            <Select label="Cultivation Status" {...register("cultivationStatus")}>
              <option value="PLANNED">Planned</option>
              <option value="GROWING">Growing</option>
              <option value="HARVESTED">Harvested</option>
              <option value="CANCELLED">Cancelled</option>
            </Select>
          </div>

          <div className="grid gap-4 sm:grid-cols-3">
            <Input label="Sowing Date" type="date" {...register("sowingDate")} />
            <Input label="Expected Harvest" type="date" {...register("expectedHarvestDate")} />
            <Input label="Actual Harvest" type="date" {...register("actualHarvestDate")} />
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Expected Yield (kg, estimate)" type="number" step="any" {...register("expectedYieldKg")} />
            <Input label="Actual Harvest Yield (kg)" type="number" step="any" {...register("actualYieldKg")} />
          </div>

          <Textarea label="Field Observations" {...register("fieldObservations")} />

          <div className="flex justify-end gap-3">
            <Button variant="outline" onClick={() => setModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={isSubmitting || saveMutation.isPending}>
              Save Crop
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(deleting)}
        onClose={() => setDeleting(null)}
        onConfirm={() => deleting && deleteMutation.mutate(deleting.id)}
        loading={deleteMutation.isPending}
        title="Delete Crop Record?"
        description={`Remove "${deleting?.cropName}" planting record?`}
        confirmLabel="Delete"
      />
    </div>
  );
}
