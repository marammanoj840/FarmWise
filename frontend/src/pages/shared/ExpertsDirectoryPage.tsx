import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Calendar, CheckCircle2, Clock, MessageSquare, Search, ShieldAlert } from "lucide-react";
import { expertApi, farmApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage, formatCurrencyINR, formatDateTime } from "@/utils/format";
import type { ExpertProfile } from "@/types/api";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Breadcrumbs } from "@/components/ui/Feedback";

export function ExpertsDirectoryPage() {
  const queryClient = useQueryClient();
  const { success, error } = useToast();
  const [search, setSearch] = useState("");
  const [verifiedOnly, setVerifiedOnly] = useState(false);
  const [consultTarget, setConsultTarget] = useState<ExpertProfile | null>(null);
  const [apptTarget, setApptTarget] = useState<ExpertProfile | null>(null);

  const { data: experts } = useQuery({
    queryKey: ["experts", search, verifiedOnly],
    queryFn: () => expertApi.list(search || undefined, verifiedOnly || undefined),
  });
  const { data: farms } = useQuery({ queryKey: ["farms", "all"], queryFn: () => farmApi.list(undefined, 0, 50) });
  const { data: consultations } = useQuery({ queryKey: ["consultations", "mine"], queryFn: expertApi.myConsultations });
  const { data: appointments } = useQuery({ queryKey: ["consultations", "appointments"], queryFn: expertApi.appointments });

  const consultForm = useForm({
    defaultValues: { farmId: "", topic: "", cropName: "", question: "", urgency: "NORMAL" },
  });

  const [defaultStart] = useState(() => new Date(Date.now() + 86400000).toISOString().slice(0, 16));

  const apptForm = useForm({
    defaultValues: {
      scheduledStart: defaultStart,
      meetingMode: "VIDEO",
      meetingNotes: "",
    },
  });

  const createConsult = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      expertApi.createConsultation({ ...v, expertId: consultTarget!.id, farmId: v.farmId || null }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["consultations", "mine"] });
      setConsultTarget(null);
      consultForm.reset();
      success("Consultation request submitted");
    },
    onError: (err) => error("Failed to submit", extractErrorMessage(err)),
  });

  const createAppt = useMutation({
    mutationFn: (v: Record<string, unknown>) => {
      const start = new Date(String(v.scheduledStart));
      const end = new Date(start.getTime() + 30 * 60000);
      return expertApi.scheduleAppointment({
        expertId: apptTarget!.id,
        scheduledStart: start.toISOString(),
        scheduledEnd: end.toISOString(),
        meetingMode: v.meetingMode,
        meetingNotes: v.meetingNotes,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["consultations", "appointments"] });
      setApptTarget(null);
      success("Appointment scheduled");
    },
    onError: (err) => error("Scheduling failed", extractErrorMessage(err)),
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Expert Directory & Consultations" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Agricultural Expert Directory</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Connect with agronomists and specialists. Verification badges appear only after Admin review.
          </p>
        </div>
      </div>

      <Card className="flex flex-wrap items-center gap-4">
        <div className="relative min-w-60 flex-1">
          <Search className="pointer-events-none absolute left-3.5 top-3.5 h-4 w-4 text-[#6B8277]" />
          <input
            type="search"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by specialization (e.g., Soil, Wheat, Cotton)..."
            className="min-h-11 w-full rounded-xl border border-[#C9D6CA] bg-white pl-10 pr-4 text-sm dark:border-[#244337] dark:bg-[#0D1E17]"
          />
        </div>
        <label className="inline-flex items-center gap-2 text-sm font-medium">
          <input
            type="checkbox"
            checked={verifiedOnly}
            onChange={(e) => setVerifiedOnly(e.target.checked)}
            className="h-4 w-4 accent-[#10402E]"
          />
          Verified Experts Only
        </label>
      </Card>

      <div className="grid gap-5 md:grid-cols-2">
        {(experts || []).map((e) => (
          <Card key={e.id} className="flex flex-col justify-between space-y-4">
            <div className="space-y-2">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h2 className="text-lg font-bold">{e.fullName}</h2>
                  <p className="text-xs font-medium text-[#2A8C55] dark:text-[#4FD085]">{e.headline}</p>
                </div>
                {e.verified ? (
                  <Badge tone="green">
                    <CheckCircle2 className="h-3 w-3" /> Verified Expert
                  </Badge>
                ) : (
                  <Badge tone="amber">
                    <ShieldAlert className="h-3 w-3" /> Verification {e.verificationStatus}
                  </Badge>
                )}
              </div>
              <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                <strong>Specializations:</strong> {e.specializations}
              </p>
              <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                {e.qualifications ? `${e.qualifications} • ` : ""}
                {e.experienceYears} yrs exp • {e.languages}
                {e.city ? ` • ${e.city}, ${e.state}` : ""}
              </p>
              {e.bio && <p className="text-xs leading-relaxed text-[#4B6358] dark:text-[#9EB7AB]">{e.bio}</p>}
            </div>

            <div className="flex flex-wrap items-center justify-between gap-2 border-t border-[#E9EFE9] pt-3 dark:border-[#1D392E]">
              <span className="text-sm font-bold">
                Fee: {e.consultationFeeInr > 0 ? formatCurrencyINR(e.consultationFeeInr) : "Free"}
              </span>
              <div className="flex gap-2">
                <Button size="sm" variant="outline" onClick={() => setApptTarget(e)} icon={<Calendar className="h-3.5 w-3.5" />}>
                  Book Slot
                </Button>
                <Button size="sm" onClick={() => setConsultTarget(e)} icon={<MessageSquare className="h-3.5 w-3.5" />}>
                  Ask Question
                </Button>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Advisory History & Appointments */}
      <div className="grid gap-6 lg:grid-cols-2">
        <Card className="space-y-3">
          <h2 className="text-lg font-bold">My Consultation History</h2>
          {(consultations || []).map((c) => (
            <div key={c.id} className="rounded-xl border border-[#DCE5DC] p-3.5 text-xs dark:border-[#203D31]">
              <div className="flex items-center justify-between">
                <span className="font-bold">{c.topic}</span>
                <Badge tone={c.status === "ANSWERED" ? "green" : "amber"}>{c.status}</Badge>
              </div>
              <p className="mt-1 text-[#4B6358] dark:text-[#9EB7AB]">
                Expert: {c.expertName} ({c.expertVerified ? "Verified" : "Pending Verification"})
              </p>
              <p className="mt-1.5">{c.question}</p>
              {c.expertResponse && (
                <div className="mt-2 rounded-lg bg-[#EEF3EE] p-2.5 dark:bg-[#152B22]">
                  <strong>Expert Advice:</strong> {c.expertResponse}
                </div>
              )}
            </div>
          ))}
        </Card>

        <Card className="space-y-3">
          <h2 className="text-lg font-bold">Scheduled Advisory Appointments</h2>
          {(appointments || []).map((a) => (
            <div key={a.id} className="flex items-center justify-between rounded-xl border border-[#DCE5DC] p-3.5 text-xs dark:border-[#203D31]">
              <div>
                <p className="font-bold">{a.expertName}</p>
                <p className="mt-0.5 inline-flex items-center gap-1 text-[#4B6358] dark:text-[#9EB7AB]">
                  <Clock className="h-3 w-3" /> {formatDateTime(a.scheduledStart)} • {a.meetingMode}
                </p>
              </div>
              <Badge tone="blue">{a.status}</Badge>
            </div>
          ))}
        </Card>
      </div>

      <Modal
        open={Boolean(consultTarget)}
        onClose={() => setConsultTarget(null)}
        title={`Ask ${consultTarget?.fullName || ""}`}
      >
        <form onSubmit={consultForm.handleSubmit((v) => createConsult.mutate(v))} className="space-y-4">
          <Select label="Related Farm (Optional)" {...consultForm.register("farmId")}>
            <option value="">General question</option>
            {(farms?.items || []).map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </Select>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Topic" required {...consultForm.register("topic", { required: true })} />
            <Input label="Crop Name" {...consultForm.register("cropName")} />
          </div>
          <Select label="Urgency" {...consultForm.register("urgency")}>
            <option value="NORMAL">Normal</option>
            <option value="HIGH">High</option>
          </Select>
          <Textarea label="Detailed Question" required {...consultForm.register("question", { required: true })} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setConsultTarget(null)}>
              Cancel
            </Button>
            <Button type="submit" loading={createConsult.isPending}>
              Submit Question
            </Button>
          </div>
        </form>
      </Modal>

      <Modal
        open={Boolean(apptTarget)}
        onClose={() => setApptTarget(null)}
        title={`Schedule 30-Min Session with ${apptTarget?.fullName || ""}`}
      >
        <form onSubmit={apptForm.handleSubmit((v) => createAppt.mutate(v))} className="space-y-4">
          <Input
            label="Preferred Date & Time"
            type="datetime-local"
            required
            {...apptForm.register("scheduledStart", { required: true })}
          />
          <Select label="Meeting Mode" {...apptForm.register("meetingMode")}>
            <option value="VIDEO">Video Call</option>
            <option value="PHONE">Phone Call</option>
            <option value="FIELD_VISIT">Field Visit Request</option>
          </Select>
          <Textarea label="Notes / Agenda" {...apptForm.register("meetingNotes")} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setApptTarget(null)}>
              Cancel
            </Button>
            <Button type="submit" loading={createAppt.isPending}>
              Confirm Slot
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
