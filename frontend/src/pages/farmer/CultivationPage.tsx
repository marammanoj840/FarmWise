import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { CalendarCheck, CheckCircle2, Plus, Trash2 } from "lucide-react";
import { cropApi, cultivationApi, farmApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage, formatCurrencyINR, formatDate } from "@/utils/format";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Breadcrumbs, EmptyState } from "@/components/ui/Feedback";

const STAGES = [
  "LAND_PREPARATION",
  "SEED_SELECTION",
  "SOWING",
  "IRRIGATION",
  "FERTILIZATION",
  "PEST_MANAGEMENT",
  "DISEASE_MANAGEMENT",
  "CROP_MONITORING",
  "HARVESTING",
];

export function CultivationPage() {
  const queryClient = useQueryClient();
  const { success, error } = useToast();
  const [tab, setTab] = useState<"tasks" | "activities">("tasks");
  const [taskModal, setTaskModal] = useState(false);
  const [activityModal, setActivityModal] = useState(false);

  const { data: farms } = useQuery({ queryKey: ["farms", "all"], queryFn: () => farmApi.list(undefined, 0, 50) });
  const { data: crops } = useQuery({ queryKey: ["crops", "all"], queryFn: () => cropApi.list(undefined, 0, 50) });
  const { data: tasks } = useQuery({
    queryKey: ["cultivation", "tasks"],
    queryFn: () => cultivationApi.listTasks(undefined, 0, 50),
  });
  const { data: activities } = useQuery({
    queryKey: ["cultivation", "activities"],
    queryFn: () => cultivationApi.listActivities(undefined, 0, 50),
  });

  const taskForm = useForm({
    defaultValues: {
      farmId: "",
      farmCropId: "",
      stage: "IRRIGATION",
      title: "",
      dueDate: new Date().toISOString().slice(0, 10),
      priority: "MEDIUM",
      notes: "",
    },
  });

  const actForm = useForm({
    defaultValues: {
      farmId: "",
      farmCropId: "",
      stage: "SOWING",
      title: "",
      activityDate: new Date().toISOString().slice(0, 10),
      inputUsed: "",
      quantity: "",
      unit: "kg",
      costAmount: "0",
      notes: "",
    },
  });

  const createTaskMutation = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      cultivationApi.createTask({ ...v, farmCropId: v.farmCropId || null }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cultivation", "tasks"] });
      queryClient.invalidateQueries({ queryKey: ["farmer", "dashboard"] });
      setTaskModal(false);
      taskForm.reset();
      success("Task scheduled");
    },
    onError: (err) => error("Failed to schedule task", extractErrorMessage(err)),
  });

  const toggleStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => cultivationApi.updateTaskStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cultivation", "tasks"] });
      queryClient.invalidateQueries({ queryKey: ["farmer", "dashboard"] });
      success("Task status updated");
    },
  });

  const createActMutation = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      cultivationApi.createActivity({
        ...v,
        farmCropId: v.farmCropId || null,
        quantity: v.quantity ? Number(v.quantity) : null,
        costAmount: v.costAmount ? Number(v.costAmount) : 0,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cultivation", "activities"] });
      queryClient.invalidateQueries({ queryKey: ["farmer", "dashboard"] });
      setActivityModal(false);
      actForm.reset();
      success("Cultivation activity logged");
    },
    onError: (err) => error("Failed to log activity", extractErrorMessage(err)),
  });

  const deleteActivityMutation = useMutation({
    mutationFn: cultivationApi.deleteActivity,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cultivation", "activities"] });
      success("Activity deleted");
    },
  });

  const deleteTaskMutation = useMutation({
    mutationFn: cultivationApi.deleteTask,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cultivation", "tasks"] });
      success("Task deleted");
    },
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Dashboard", to: "/app/farmer" }, { label: "Cultivation Management" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Cultivation Tracking</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Plan reminders and record field operations across all 9 cultivation lifecycle stages.
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button variant="outline" onClick={() => setTaskModal(true)} icon={<Plus className="h-4 w-4" />}>
            Schedule Task
          </Button>
          <Button onClick={() => setActivityModal(true)} icon={<Plus className="h-4 w-4" />}>
            Log Field Activity
          </Button>
        </div>
      </div>

      <div className="flex gap-2 border-b border-[#DCE5DC] pb-2 dark:border-[#203D31]">
        <button
          type="button"
          onClick={() => setTab("tasks")}
          className={`rounded-xl px-4 py-2 text-sm font-semibold ${
            tab === "tasks"
              ? "bg-[#10402E] text-white dark:bg-[#2A8C55]"
              : "text-[#4B6358] hover:bg-[#EEF3EE] dark:text-[#9EB7AB]"
          }`}
        >
          Scheduled Tasks ({tasks?.items.length ?? 0})
        </button>
        <button
          type="button"
          onClick={() => setTab("activities")}
          className={`rounded-xl px-4 py-2 text-sm font-semibold ${
            tab === "activities"
              ? "bg-[#10402E] text-white dark:bg-[#2A8C55]"
              : "text-[#4B6358] hover:bg-[#EEF3EE] dark:text-[#9EB7AB]"
          }`}
        >
          Activity History ({activities?.items.length ?? 0})
        </button>
      </div>

      {tab === "tasks" ? (
        !tasks || tasks.items.length === 0 ? (
          <EmptyState
            icon={<CalendarCheck className="h-6 w-6" />}
            title="No cultivation tasks scheduled"
            description="Schedule irrigation, fertilization, pest scouting, or harvest reminders."
          />
        ) : (
          <div className="space-y-3">
            {tasks.items.map((t) => (
              <Card key={t.id} padding="p-4" className="flex flex-wrap items-center justify-between gap-4">
                <div className="space-y-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className={`font-bold ${t.status === "COMPLETED" ? "line-through opacity-60" : ""}`}>
                      {t.title}
                    </span>
                    <Badge tone={t.status === "COMPLETED" ? "green" : "amber"}>{t.status}</Badge>
                    <Badge tone="slate">{t.stage}</Badge>
                  </div>
                  <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                    Due: {formatDate(t.dueDate)} • Priority: {t.priority}
                    {t.notes ? ` • ${t.notes}` : ""}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <Button
                    size="sm"
                    variant={t.status === "COMPLETED" ? "outline" : "secondary"}
                    onClick={() =>
                      toggleStatusMutation.mutate({
                        id: t.id,
                        status: t.status === "COMPLETED" ? "PENDING" : "COMPLETED",
                      })
                    }
                    icon={<CheckCircle2 className="h-4 w-4" />}
                  >
                    {t.status === "COMPLETED" ? "Reopen" : "Complete"}
                  </Button>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => deleteTaskMutation.mutate(t.id)}
                    className="text-red-600"
                    icon={<Trash2 className="h-4 w-4" />}
                  />
                </div>
              </Card>
            ))}
          </div>
        )
      ) : !activities || activities.items.length === 0 ? (
        <EmptyState
          icon={<CalendarCheck className="h-6 w-6" />}
          title="No activities recorded"
          description="Log land preparation, sowing, irrigation, fertilization, or harvest events."
        />
      ) : (
        <div className="space-y-3">
          {activities.items.map((a) => (
            <Card key={a.id} padding="p-4" className="flex flex-wrap items-center justify-between gap-4">
              <div>
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-bold">{a.title}</span>
                  <Badge tone="green">{a.stage}</Badge>
                </div>
                <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                  Date: {formatDate(a.activityDate)}
                  {a.inputUsed ? ` • Input: ${a.inputUsed}` : ""}
                  {a.quantity ? ` (${a.quantity} ${a.unit || ""})` : ""} • Cost: {formatCurrencyINR(a.costAmount)}
                </p>
                {a.notes && <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">{a.notes}</p>}
              </div>
              <Button
                size="sm"
                variant="ghost"
                onClick={() => deleteActivityMutation.mutate(a.id)}
                className="text-red-600"
                icon={<Trash2 className="h-4 w-4" />}
              />
            </Card>
          ))}
        </div>
      )}

      {/* Schedule Task Modal */}
      <Modal open={taskModal} onClose={() => setTaskModal(false)} title="Schedule Cultivation Task">
        <form onSubmit={taskForm.handleSubmit((v) => createTaskMutation.mutate(v))} className="space-y-4">
          <Select label="Farm" required {...taskForm.register("farmId", { required: true })}>
            <option value="">Select farm...</option>
            {(farms?.items || []).map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </Select>
          <Select label="Crop (Optional)" {...taskForm.register("farmCropId")}>
            <option value="">General farm task</option>
            {(crops?.items || []).map((c) => (
              <option key={c.id} value={c.id}>
                {c.cropName} ({c.farmName})
              </option>
            ))}
          </Select>
          <div className="grid gap-4 sm:grid-cols-2">
            <Select label="Stage" {...taskForm.register("stage")}>
              {STAGES.map((s) => (
                <option key={s} value={s}>
                  {s.replace(/_/g, " ")}
                </option>
              ))}
            </Select>
            <Select label="Priority" {...taskForm.register("priority")}>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </Select>
          </div>
          <Input label="Task Title" required {...taskForm.register("title", { required: true })} />
          <Input label="Due Date" type="date" required {...taskForm.register("dueDate", { required: true })} />
          <Textarea label="Notes" {...taskForm.register("notes")} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setTaskModal(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={createTaskMutation.isPending}>
              Schedule Task
            </Button>
          </div>
        </form>
      </Modal>

      {/* Log Activity Modal */}
      <Modal open={activityModal} onClose={() => setActivityModal(false)} title="Log Cultivation Activity">
        <form onSubmit={actForm.handleSubmit((v) => createActMutation.mutate(v))} className="space-y-4">
          <Select label="Farm" required {...actForm.register("farmId", { required: true })}>
            <option value="">Select farm...</option>
            {(farms?.items || []).map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </Select>
          <Select label="Crop (Optional)" {...actForm.register("farmCropId")}>
            <option value="">General farm operation</option>
            {(crops?.items || []).map((c) => (
              <option key={c.id} value={c.id}>
                {c.cropName} ({c.farmName})
              </option>
            ))}
          </Select>
          <div className="grid gap-4 sm:grid-cols-2">
            <Select label="Cultivation Stage" {...actForm.register("stage")}>
              {STAGES.map((s) => (
                <option key={s} value={s}>
                  {s.replace(/_/g, " ")}
                </option>
              ))}
            </Select>
            <Input label="Date" type="date" required {...actForm.register("activityDate", { required: true })} />
          </div>
          <Input label="Activity Title" required {...actForm.register("title", { required: true })} />
          <div className="grid gap-4 sm:grid-cols-3">
            <Input label="Input Material" {...actForm.register("inputUsed")} />
            <Input label="Quantity" type="number" step="any" {...actForm.register("quantity")} />
            <Input label="Unit" {...actForm.register("unit")} />
          </div>
          <Input label="Cost (INR ₹)" type="number" step="0.01" {...actForm.register("costAmount")} />
          <Textarea label="Notes" {...actForm.register("notes")} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setActivityModal(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={createActMutation.isPending}>
              Save Activity
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
