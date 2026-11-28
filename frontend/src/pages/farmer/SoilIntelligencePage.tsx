import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { AlertTriangle, FlaskConical, Plus, Trash2 } from "lucide-react";
import { cropApi, farmApi, soilApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage, formatDate } from "@/utils/format";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Breadcrumbs, EmptyState } from "@/components/ui/Feedback";

export function SoilIntelligencePage() {
  const queryClient = useQueryClient();
  const { success, error } = useToast();
  const [tab, setTab] = useState<"soil" | "observations" | "suitability" | "knowledge">("soil");
  const [soilModal, setSoilModal] = useState(false);
  const [obsModal, setObsModal] = useState(false);
  const [suitSoil, setSuitSoil] = useState("Alluvial Loam");
  const [suitPh, setSuitPh] = useState(6.8);

  const { data: farms } = useQuery({ queryKey: ["farms", "all"], queryFn: () => farmApi.list(undefined, 0, 50) });
  const { data: crops } = useQuery({ queryKey: ["crops", "all"], queryFn: () => cropApi.list(undefined, 0, 50) });
  const { data: soilRecords } = useQuery({ queryKey: ["soil", "records"], queryFn: () => soilApi.listRecords(0, 50) });
  const { data: observations } = useQuery({
    queryKey: ["soil", "observations"],
    queryFn: () => soilApi.listObservations(0, 50),
  });
  const { data: knowledge } = useQuery({ queryKey: ["soil", "knowledge"], queryFn: () => soilApi.knowledgeBase() });
  const { data: suitability } = useQuery({
    queryKey: ["soil", "suitability", suitSoil, suitPh],
    queryFn: () => soilApi.suitability(suitSoil, suitPh),
  });

  const soilForm = useForm({
    defaultValues: {
      farmId: "",
      soilType: "Alluvial Loam",
      ph: 6.8,
      nitrogen: 240,
      phosphorus: 22,
      potassium: 185,
      organicCarbonPct: 0.55,
      moisturePct: 24,
      fertilityRating: "GOOD",
      labName: "",
      testDate: new Date().toISOString().slice(0, 10),
      observations: "",
    },
  });

  const obsForm = useForm({
    defaultValues: {
      farmId: "",
      farmCropId: "",
      observationType: "PEST",
      severity: "MEDIUM",
      title: "",
      symptoms: "",
      affectedAreaPct: 10,
      recommendedAction: "",
      observedAt: new Date().toISOString().slice(0, 10),
    },
  });

  const createSoil = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      soilApi.createRecord({
        ...v,
        ph: Number(v.ph),
        nitrogen: v.nitrogen ? Number(v.nitrogen) : null,
        phosphorus: v.phosphorus ? Number(v.phosphorus) : null,
        potassium: v.potassium ? Number(v.potassium) : null,
        organicCarbonPct: v.organicCarbonPct ? Number(v.organicCarbonPct) : null,
        moisturePct: v.moisturePct ? Number(v.moisturePct) : null,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["soil", "records"] });
      setSoilModal(false);
      success("Soil record saved");
    },
    onError: (err) => error("Failed to save soil test", extractErrorMessage(err)),
  });

  const deleteSoil = useMutation({
    mutationFn: soilApi.deleteRecord,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["soil", "records"] }),
  });

  const createObs = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      soilApi.createObservation({
        ...v,
        farmCropId: v.farmCropId || null,
        affectedAreaPct: v.affectedAreaPct ? Number(v.affectedAreaPct) : null,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["soil", "observations"] });
      setObsModal(false);
      success("Observation logged");
    },
    onError: (err) => error("Failed to save observation", extractErrorMessage(err)),
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Dashboard", to: "/app/farmer" }, { label: "Soil & Crop Intelligence" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Soil & Crop Intelligence</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Maintain lab soil test history, pest/disease observations, and crop suitability guidance.
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => setObsModal(true)} icon={<Plus className="h-4 w-4" />}>
            Log Pest / Health Observation
          </Button>
          <Button onClick={() => setSoilModal(true)} icon={<Plus className="h-4 w-4" />}>
            Add Soil Record
          </Button>
        </div>
      </div>

      <div className="flex flex-wrap gap-2 border-b border-[#DCE5DC] pb-2 dark:border-[#203D31]">
        {[
          { id: "soil", label: "Soil Test History" },
          { id: "observations", label: "Crop Health & Pests" },
          { id: "suitability", label: "Crop Suitability Checker" },
          { id: "knowledge", label: "Crop Knowledge Base" },
        ].map((t) => (
          <button
            key={t.id}
            type="button"
            onClick={() => setTab(t.id as typeof tab)}
            className={`rounded-xl px-4 py-2 text-sm font-semibold ${
              tab === t.id
                ? "bg-[#10402E] text-white dark:bg-[#2A8C55]"
                : "text-[#4B6358] hover:bg-[#EEF3EE] dark:text-[#9EB7AB]"
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === "soil" && (
        !soilRecords || soilRecords.items.length === 0 ? (
          <EmptyState
            icon={<FlaskConical className="h-6 w-6" />}
            title="No soil test records yet"
            description="Record your laboratory soil test results to track NPK nutrients, soil pH, organic carbon, and receive tailored crop suitability guidance."
            action={
              <Button size="sm" onClick={() => setSoilModal(true)} icon={<Plus className="h-4 w-4" />}>
                Add First Soil Record
              </Button>
            }
          />
        ) : (
          <div className="grid gap-4 md:grid-cols-2">
            {(soilRecords?.items || []).map((r) => (
              <Card key={r.id} className="space-y-3">
                <div className="flex items-start justify-between">
                  <div>
                    <h2 className="font-bold">{r.farmName}</h2>
                    <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                      {r.soilType} • Tested {formatDate(r.testDate)} {r.labName ? `• ${r.labName}` : ""}
                    </p>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Badge tone="green">pH {r.ph}</Badge>
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => deleteSoil.mutate(r.id)}
                      icon={<Trash2 className="h-3.5 w-3.5 text-red-600" />}
                    />
                  </div>
                </div>
                <div className="grid grid-cols-4 gap-2 rounded-xl bg-[#EEF3EE] p-3 text-center text-xs dark:bg-[#152B22]">
                  <div><p className="text-[#4B6358]">N</p><p className="font-bold">{r.nitrogen ?? "—"}</p></div>
                  <div><p className="text-[#4B6358]">P</p><p className="font-bold">{r.phosphorus ?? "—"}</p></div>
                  <div><p className="text-[#4B6358]">K</p><p className="font-bold">{r.potassium ?? "—"}</p></div>
                  <div><p className="text-[#4B6358]">Org C%</p><p className="font-bold">{r.organicCarbonPct ?? "—"}</p></div>
                </div>
                {r.observations && <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">{r.observations}</p>}
              </Card>
            ))}
          </div>
        )
      )}

      {tab === "observations" && (
        <div className="space-y-4">
          <div className="flex items-start gap-2 rounded-xl border border-amber-200 bg-amber-50 p-3 text-xs text-amber-950 dark:border-amber-900 dark:bg-amber-950/30 dark:text-amber-200">
            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
            <span>
              Field observations logged here are self-recorded and NOT professionally verified diagnoses. Consult an
              approved agricultural expert before applying chemical treatments.
            </span>
          </div>
          {!observations || observations.items.length === 0 ? (
            <EmptyState
              icon={<AlertTriangle className="h-6 w-6" />}
              title="No field observations logged"
              description="Track pest sightings, leaf symptoms, disease outbreaks, and field health observations across your farm plots."
              action={
                <Button size="sm" variant="outline" onClick={() => setObsModal(true)} icon={<Plus className="h-4 w-4" />}>
                  Log First Observation
                </Button>
              }
            />
          ) : (
            <div className="grid gap-4 md:grid-cols-2">
              {(observations?.items || []).map((o) => (
                <Card key={o.id} className="space-y-2">
                  <div className="flex items-center justify-between">
                    <h3 className="font-bold">{o.title}</h3>
                    <div className="flex gap-1.5">
                      <Badge tone={o.severity === "HIGH" ? "red" : "amber"}>{o.severity}</Badge>
                      <Badge tone={o.expertVerified ? "green" : "slate"}>
                        {o.expertVerified ? "Expert Verified" : "Unverified Log"}
                      </Badge>
                    </div>
                  </div>
                  <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                    {o.observationType} • Observed {formatDate(o.observedAt)}
                  </p>
                  <p className="text-sm">{o.symptoms}</p>
                  {o.recommendedAction && (
                    <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                      <strong>Action Note:</strong> {o.recommendedAction}
                    </p>
                  )}
                </Card>
              ))}
            </div>
          )}
        </div>
      )}

      {tab === "suitability" && (
        <div className="space-y-4">
          <Card className="grid gap-4 sm:grid-cols-2">
            <Input label="Soil Type" value={suitSoil} onChange={(e) => setSuitSoil(e.target.value)} />
            <Input
              label="Measured Soil pH (0 - 14)"
              type="number"
              step="0.1"
              min={0}
              max={14}
              value={suitPh}
              onChange={(e) => setSuitPh(Number(e.target.value))}
            />
          </Card>

          {suitability && (
            <Card className="space-y-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <FlaskConical className="h-5 w-5 text-[#2A8C55]" />
                  <h2 className="text-lg font-bold">Reference Crops Compatible with pH {suitability.ph}</h2>
                </div>
                <Badge tone="amber">Advisory Only — Not Professionally Verified</Badge>
              </div>
              <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">{suitability.uncertaintyDisclaimer}</p>
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {suitability.candidates.map((c) => (
                  <div key={c.cropName} className="rounded-xl border border-[#DCE5DC] p-3.5 text-xs dark:border-[#203D31]">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-bold">{c.cropName}</span>
                      <Badge tone="green">pH {c.phRange}</Badge>
                    </div>
                    <p className="mt-1 text-[#4B6358] dark:text-[#9EB7AB]">{c.seasonSuitability}</p>
                    <p className="mt-2">{c.matchConfidenceNote}</p>
                  </div>
                ))}
              </div>
            </Card>
          )}
        </div>
      )}

      {tab === "knowledge" && (
        <div className="grid gap-4 md:grid-cols-2">
          {(knowledge || []).map((k) => (
            <Card key={k.id} className="space-y-2 text-sm">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-bold">{k.cropName}</h3>
                <Badge tone="green">pH {k.idealPhRange}</Badge>
              </div>
              <p><strong>Soils:</strong> {k.suitableSoilTypes}</p>
              <p><strong>Irrigation:</strong> {k.irrigationSchedule}</p>
              <p><strong>Nutrients:</strong> {k.nutrientGuidance}</p>
              <p><strong>Pests & Diseases:</strong> {k.commonPestsDiseases}</p>
            </Card>
          ))}
        </div>
      )}

      {/* Soil Modal */}
      <Modal open={soilModal} onClose={() => setSoilModal(false)} title="Add Soil Test Record">
        <form onSubmit={soilForm.handleSubmit((v) => createSoil.mutate(v))} className="space-y-4">
          <Select label="Farm" required {...soilForm.register("farmId", { required: true })}>
            <option value="">Select farm...</option>
            {(farms?.items || []).map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </Select>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Soil Type" required {...soilForm.register("soilType", { required: true })} />
            <Input label="Soil pH" type="number" step="0.1" required {...soilForm.register("ph", { required: true })} />
          </div>
          <div className="grid gap-4 sm:grid-cols-3">
            <Input label="Nitrogen (kg/ha)" type="number" step="any" {...soilForm.register("nitrogen")} />
            <Input label="Phosphorus (kg/ha)" type="number" step="any" {...soilForm.register("phosphorus")} />
            <Input label="Potassium (kg/ha)" type="number" step="any" {...soilForm.register("potassium")} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Lab Name" {...soilForm.register("labName")} />
            <Input label="Test Date" type="date" required {...soilForm.register("testDate", { required: true })} />
          </div>
          <Textarea label="Fertility Observations" {...soilForm.register("observations")} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setSoilModal(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={createSoil.isPending}>
              Save Soil Record
            </Button>
          </div>
        </form>
      </Modal>

      {/* Observation Modal */}
      <Modal open={obsModal} onClose={() => setObsModal(false)} title="Log Crop Health / Pest Observation">
        <form onSubmit={obsForm.handleSubmit((v) => createObs.mutate(v))} className="space-y-4">
          <Select label="Farm" required {...obsForm.register("farmId", { required: true })}>
            <option value="">Select farm...</option>
            {(farms?.items || []).map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </Select>
          <Select label="Crop (Optional)" {...obsForm.register("farmCropId")}>
            <option value="">General plot</option>
            {(crops?.items || []).map((c) => (
              <option key={c.id} value={c.id}>
                {c.cropName}
              </option>
            ))}
          </Select>
          <div className="grid gap-4 sm:grid-cols-2">
            <Select label="Observation Type" {...obsForm.register("observationType")}>
              <option value="PEST">Pest</option>
              <option value="DISEASE">Disease</option>
              <option value="NUTRIENT_DEFICIENCY">Nutrient Deficiency</option>
              <option value="HEALTH_CHECK">Routine Health Check</option>
            </Select>
            <Select label="Severity" {...obsForm.register("severity")}>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </Select>
          </div>
          <Input label="Title" required {...obsForm.register("title", { required: true })} />
          <Textarea label="Observed Symptoms" required {...obsForm.register("symptoms", { required: true })} />
          <Input label="Date Observed" type="date" required {...obsForm.register("observedAt", { required: true })} />
          <Textarea label="Preliminary Action / Notes" {...obsForm.register("recommendedAction")} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setObsModal(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={createObs.isPending}>
              Save Observation
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
