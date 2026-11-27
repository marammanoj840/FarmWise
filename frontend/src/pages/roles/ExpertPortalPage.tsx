import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { CheckCircle2, MessageSquare, ShieldAlert } from "lucide-react";
import { expertApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage, formatDateTime } from "@/utils/format";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Textarea } from "@/components/ui/Field";
import { Breadcrumbs } from "@/components/ui/Feedback";

export function ExpertPortalPage() {
  const queryClient = useQueryClient();
  const { success, error } = useToast();
  const [replyDrafts, setReplyDrafts] = useState<Record<string, string>>({});

  const { data: profile } = useQuery({ queryKey: ["expert", "my-profile"], queryFn: expertApi.myProfile });
  const { data: consultations } = useQuery({
    queryKey: ["expert", "assigned-consultations"],
    queryFn: expertApi.assignedConsultations,
  });
  const { data: appointments } = useQuery({ queryKey: ["consultations", "appointments"], queryFn: expertApi.appointments });

  const form = useForm({
    defaultValues: {
      headline: "",
      specializations: "",
      qualifications: "",
      experienceYears: 5,
      languages: "English, Hindi",
      city: "",
      state: "",
      consultationFeeInr: 0,
      bio: "",
      available: true,
    },
  });

  useEffect(() => {
    if (profile) {
      form.reset({
        headline: profile.headline || "",
        specializations: profile.specializations || "",
        qualifications: profile.qualifications || "",
        experienceYears: profile.experienceYears ?? 0,
        languages: profile.languages || "English, Hindi",
        city: profile.city || "",
        state: profile.state || "",
        consultationFeeInr: profile.consultationFeeInr ?? 0,
        bio: profile.bio || "",
        available: profile.available,
      });
    }
  }, [profile, form]);

  const updateProfile = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      expertApi.updateMyProfile({
        ...v,
        experienceYears: Number(v.experienceYears),
        consultationFeeInr: Number(v.consultationFeeInr),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["expert"] });
      success("Expert profile updated");
    },
    onError: (err) => error("Update failed", extractErrorMessage(err)),
  });

  const respondMutation = useMutation({
    mutationFn: ({ id, expertResponse }: { id: string; expertResponse: string }) =>
      expertApi.respondConsultation(id, { status: "ANSWERED", expertResponse }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["expert", "assigned-consultations"] });
      success("Advisory response sent to farmer");
    },
    onError: (err) => error("Failed to send response", extractErrorMessage(err)),
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Expert Workspace" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Agricultural Expert Portal</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Respond to farmer consultation requests, manage appointments, and update credentials.
          </p>
        </div>
        {profile && (
          <Badge tone={profile.verified ? "green" : "amber"}>
            {profile.verified ? (
              <>
                <CheckCircle2 className="h-3.5 w-3.5" /> Admin Verified
              </>
            ) : (
              <>
                <ShieldAlert className="h-3.5 w-3.5" /> Verification {profile.verificationStatus}
              </>
            )}
          </Badge>
        )}
      </div>

      <div className="grid gap-6 lg:grid-cols-12">
        <Card className="space-y-4 lg:col-span-7">
          <h2 className="inline-flex items-center gap-2 text-lg font-bold">
            <MessageSquare className="h-5 w-5 text-[#2A8C55]" />
            Farmer Consultation Requests
          </h2>
          {(consultations || []).map((c) => (
            <div key={c.id} className="space-y-2.5 rounded-xl border border-[#DCE5DC] p-4 text-sm dark:border-[#203D31]">
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-bold">{c.topic}</p>
                  <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                    Farmer: {c.farmerName} {c.cropName ? `• Crop: ${c.cropName}` : ""} • Urgency: {c.urgency}
                  </p>
                </div>
                <Badge tone={c.status === "ANSWERED" ? "green" : "amber"}>{c.status}</Badge>
              </div>
              <p className="text-xs leading-relaxed">{c.question}</p>
              {c.expertResponse ? (
                <div className="rounded-lg bg-[#EEF3EE] p-3 text-xs dark:bg-[#152B22]">
                  <strong>Your Response:</strong> {c.expertResponse}
                </div>
              ) : (
                <div className="space-y-2 pt-2">
                  <textarea
                    rows={2}
                    placeholder="Write detailed agronomic guidance..."
                    value={replyDrafts[c.id] || ""}
                    onChange={(e) => setReplyDrafts((p) => ({ ...p, [c.id]: e.target.value }))}
                    className="w-full rounded-xl border border-[#C9D6CA] p-2.5 text-xs dark:border-[#244337] dark:bg-[#0D1E17]"
                  />
                  <Button
                    size="sm"
                    disabled={!replyDrafts[c.id]?.trim()}
                    loading={respondMutation.isPending}
                    onClick={() => respondMutation.mutate({ id: c.id, expertResponse: replyDrafts[c.id] })}
                  >
                    Send Verified Advisory Response
                  </Button>
                </div>
              )}
            </div>
          ))}
          {(!consultations || consultations.length === 0) && (
            <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">No consultation requests assigned yet.</p>
          )}
        </Card>

        <div className="space-y-6 lg:col-span-5">
          <Card className="space-y-3">
            <h2 className="text-lg font-bold">Upcoming Appointments ({appointments?.length ?? 0})</h2>
            {(appointments || []).map((a) => (
              <div key={a.id} className="rounded-xl border border-[#DCE5DC] p-3 text-xs dark:border-[#203D31]">
                <p className="font-bold">{a.farmerName}</p>
                <p className="text-[#4B6358] dark:text-[#9EB7AB]">
                  {formatDateTime(a.scheduledStart)} • {a.meetingMode}
                </p>
              </div>
            ))}
          </Card>

          <Card className="space-y-4">
            <h2 className="text-lg font-bold">Expert Profile Details</h2>
            <form onSubmit={form.handleSubmit((v) => updateProfile.mutate(v))} className="space-y-3">
              <Input label="Professional Headline" required {...form.register("headline", { required: true })} />
              <Input label="Specializations" required {...form.register("specializations", { required: true })} />
              <Input label="Qualifications" {...form.register("qualifications")} />
              <div className="grid grid-cols-2 gap-3">
                <Input label="Years Experience" type="number" {...form.register("experienceYears")} />
                <Input label="Fee (INR ₹)" type="number" step="0.01" {...form.register("consultationFeeInr")} />
              </div>
              <Input label="Languages" {...form.register("languages")} />
              <Textarea label="Bio" {...form.register("bio")} />
              <Button type="submit" loading={updateProfile.isPending}>
                Update Expert Profile
              </Button>
            </form>
          </Card>
        </div>
      </div>
    </div>
  );
}
