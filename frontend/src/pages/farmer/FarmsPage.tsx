import { useState } from "react";
import { Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Edit, Eye, MapPin, Plus, Search, Trash2 } from "lucide-react";
import { farmApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage } from "@/utils/format";
import type { Farm } from "@/types/api";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { ConfirmDialog, Modal } from "@/components/ui/Modal";
import { Breadcrumbs, EmptyState, Pagination, Skeleton } from "@/components/ui/Feedback";

const farmSchema = z.object({
  name: z.string().min(2, "Farm name is required"),
  landArea: z.coerce.number().positive("Land area must be greater than 0"),
  areaUnit: z.string().min(1),
  soilType: z.string().min(2, "Soil type is required"),
  irrigationType: z.string().min(2, "Irrigation type is required"),
  description: z.string().optional(),
  photoUrlsText: z.string().optional(),
  villageOrCity: z.string().min(2, "Village or city is required"),
  district: z.string().min(2, "District is required"),
  state: z.string().min(2, "State is required"),
  postalCode: z.string().optional(),
  latitude: z.string().optional(),
  longitude: z.string().optional(),
});

type FarmFormValues = z.infer<typeof farmSchema>;

export function FarmsPage() {
  const queryClient = useQueryClient();
  const { success, error } = useToast();
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Farm | null>(null);
  const [deleting, setDeleting] = useState<Farm | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ["farms", search, page],
    queryFn: () => farmApi.list(search || undefined, page, 12),
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FarmFormValues>({
    resolver: zodResolver(farmSchema) as never,
    defaultValues: { areaUnit: "ACRES", soilType: "Alluvial Loam", irrigationType: "Drip" },
  });

  const openCreate = () => {
    setEditing(null);
    reset({
      name: "",
      landArea: 2.5,
      areaUnit: "ACRES",
      soilType: "Alluvial Loam",
      irrigationType: "Drip",
      description: "",
      photoUrlsText: "",
      villageOrCity: "",
      district: "",
      state: "",
      postalCode: "",
      latitude: "",
      longitude: "",
    });
    setModalOpen(true);
  };

  const openEdit = (farm: Farm) => {
    setEditing(farm);
    reset({
      name: farm.name,
      landArea: farm.landArea,
      areaUnit: farm.areaUnit,
      soilType: farm.soilType,
      irrigationType: farm.irrigationType,
      description: farm.description || "",
      photoUrlsText: (farm.photoUrls || []).join(", "),
      villageOrCity: farm.location?.villageOrCity || "",
      district: farm.location?.district || "",
      state: farm.location?.state || "",
      postalCode: farm.location?.postalCode || "",
      latitude: farm.location?.latitude != null ? String(farm.location.latitude) : "",
      longitude: farm.location?.longitude != null ? String(farm.location.longitude) : "",
    });
    setModalOpen(true);
  };

  const saveMutation = useMutation({
    mutationFn: (values: FarmFormValues) => {
      const payload = {
        name: values.name,
        landArea: values.landArea,
        areaUnit: values.areaUnit,
        soilType: values.soilType,
        irrigationType: values.irrigationType,
        description: values.description || undefined,
        photoUrls: (values.photoUrlsText || "")
          .split(",")
          .map((s) => s.trim())
          .filter(Boolean),
        location: {
          villageOrCity: values.villageOrCity,
          district: values.district,
          state: values.state,
          country: "India",
          postalCode: values.postalCode || undefined,
          latitude: values.latitude ? Number(values.latitude) : null,
          longitude: values.longitude ? Number(values.longitude) : null,
        },
      };
      return editing ? farmApi.update(editing.id, payload) : farmApi.create(payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["farms"] });
      queryClient.invalidateQueries({ queryKey: ["farmer", "dashboard"] });
      setModalOpen(false);
      success(editing ? "Farm updated" : "Farm created");
    },
    onError: (err) => error("Failed to save farm", extractErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => farmApi.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["farms"] });
      queryClient.invalidateQueries({ queryKey: ["farmer", "dashboard"] });
      setDeleting(null);
      success("Farm deleted");
    },
    onError: (err) => error("Delete failed", extractErrorMessage(err)),
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Dashboard", to: "/app/farmer" }, { label: "My Farms" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Farm Management</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Manage multiple agricultural holdings, soil profiles, irrigation setups, and locations.
          </p>
        </div>
        <Button onClick={openCreate} icon={<Plus className="h-4 w-4" />}>
          Add Farm
        </Button>
      </div>

      <div className="relative max-w-sm">
        <Search className="pointer-events-none absolute left-3.5 top-3.5 h-4 w-4 text-[#6B8277]" />
        <input
          type="search"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setPage(0);
          }}
          placeholder="Search farms by name..."
          className="min-h-11 w-full rounded-xl border border-[#C9D6CA] bg-white pl-10 pr-4 text-sm focus:border-[#2A8C55] focus:outline-none dark:border-[#244337] dark:bg-[#0D1E17]"
        />
      </div>

      {isLoading ? (
        <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
          <Skeleton className="h-48" />
          <Skeleton className="h-48" />
        </div>
      ) : !data || data.items.length === 0 ? (
        <EmptyState
          icon={<MapPin className="h-6 w-6" />}
          title="No farms added yet"
          description="Register your first farm plot to start tracking crops, weather, soil records, and expenses."
          action={<Button onClick={openCreate}>Create First Farm</Button>}
        />
      ) : (
        <>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {data.items.map((farm) => (
              <Card key={farm.id} className="flex flex-col justify-between">
                <div>
                  <div className="flex items-start justify-between gap-2">
                    <h2 className="text-lg font-bold">{farm.name}</h2>
                    <Badge tone="green">
                      {farm.landArea} {farm.areaUnit}
                    </Badge>
                  </div>
                  <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                    {farm.location?.villageOrCity}, {farm.location?.district}, {farm.location?.state}
                  </p>
                  <div className="mt-3 flex flex-wrap gap-2 text-xs">
                    <Badge tone="slate">Soil: {farm.soilType}</Badge>
                    <Badge tone="blue">Irrigation: {farm.irrigationType}</Badge>
                  </div>
                  {farm.description && (
                    <p className="mt-3 line-clamp-2 text-xs text-[#4B6358] dark:text-[#9EB7AB]">{farm.description}</p>
                  )}
                </div>

                <div className="mt-5 flex items-center justify-between gap-2 border-t border-[#E9EFE9] pt-3 dark:border-[#1D392E]">
                  <Link to={`/app/farms/${farm.id}`}>
                    <Button size="sm" variant="secondary" icon={<Eye className="h-3.5 w-3.5" />}>
                      Details
                    </Button>
                  </Link>
                  <div className="flex gap-1.5">
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => openEdit(farm)}
                      aria-label={`Edit ${farm.name}`}
                      icon={<Edit className="h-3.5 w-3.5" />}
                    >
                      Edit
                    </Button>
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => setDeleting(farm)}
                      aria-label={`Delete ${farm.name}`}
                      className="text-red-600 hover:bg-red-50 dark:text-red-400"
                      icon={<Trash2 className="h-3.5 w-3.5" />}
                    />
                  </div>
                </div>
              </Card>
            ))}
          </div>
          <Pagination page={data.page} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editing ? "Edit Farm" : "Register New Farm"}
        subtitle="Enter land details, soil type, irrigation setup, and coordinates."
      >
        <form onSubmit={handleSubmit((v) => saveMutation.mutate(v))} className="space-y-4">
          <Input label="Farm Name" required {...register("name")} error={errors.name?.message} />
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Land Area"
              type="number"
              step="0.01"
              required
              {...register("landArea")}
              error={errors.landArea?.message}
            />
            <Select label="Area Unit" required {...register("areaUnit")}>
              <option value="ACRES">Acres</option>
              <option value="HECTARES">Hectares</option>
              <option value="BIGHA">Bigha</option>
              <option value="GUNTHA">Guntha</option>
            </Select>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Soil Type" required {...register("soilType")} error={errors.soilType?.message} />
            <Input label="Irrigation Type" required {...register("irrigationType")} error={errors.irrigationType?.message} />
          </div>
          <div className="grid gap-4 sm:grid-cols-3">
            <Input label="Village / City" required {...register("villageOrCity")} error={errors.villageOrCity?.message} />
            <Input label="District" required {...register("district")} error={errors.district?.message} />
            <Input label="State" required {...register("state")} error={errors.state?.message} />
          </div>
          <div className="grid gap-4 sm:grid-cols-3">
            <Input label="Postal Code" {...register("postalCode")} />
            <Input label="Latitude (Optional)" type="number" step="any" {...register("latitude")} />
            <Input label="Longitude (Optional)" type="number" step="any" {...register("longitude")} />
          </div>
          <Input
            label="Farm Photo URLs (comma-separated, optional)"
            placeholder="https://..."
            {...register("photoUrlsText")}
          />
          <Textarea label="Description / Notes" {...register("description")} />
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="outline" onClick={() => setModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={isSubmitting || saveMutation.isPending}>
              {editing ? "Save Changes" : "Create Farm"}
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(deleting)}
        onClose={() => setDeleting(null)}
        onConfirm={() => deleting && deleteMutation.mutate(deleting.id)}
        loading={deleteMutation.isPending}
        title="Delete Farm?"
        description={`Deleting "${deleting?.name}" will also remove associated crop plantings, tasks, and soil records for this farm.`}
        confirmLabel="Delete Farm"
      />
    </div>
  );
}
