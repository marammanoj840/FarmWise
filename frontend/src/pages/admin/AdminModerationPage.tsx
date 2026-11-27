import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Bell, CheckCircle2, XCircle } from "lucide-react";
import { adminApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { formatCurrencyINR, formatDateTime } from "@/utils/format";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Breadcrumbs } from "@/components/ui/Feedback";

export function AdminModerationPage() {
  const queryClient = useQueryClient();
  const { success } = useToast();
  const [broadcastOpen, setBroadcastOpen] = useState(false);
  const [title, setTitle] = useState("");
  const [message, setMessage] = useState("");

  const { data: products } = useQuery({ queryKey: ["admin", "products"], queryFn: () => adminApi.products(0, 50) });
  const { data: produce } = useQuery({ queryKey: ["admin", "produce"], queryFn: () => adminApi.produce(0, 50) });
  const { data: contacts } = useQuery({
    queryKey: ["admin", "contacts"],
    queryFn: () => adminApi.contactInquiries(0, 50),
  });

  const moderateProd = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => adminApi.moderateProduct(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "products"] });
      success("Product moderation saved");
    },
  });

  const moderateProduce = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => adminApi.moderateProduce(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "produce"] });
      success("Produce listing moderated");
    },
  });

  const resolveContact = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      adminApi.updateContactInquiry(id, status, "Reviewed by Admin"),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "contacts"] });
      success("Inquiry updated");
    },
  });

  const broadcast = useMutation({
    mutationFn: () =>
      adminApi.broadcastNotification({
        notificationType: "SYSTEM",
        title,
        message,
        actionUrl: "/app/notifications",
      }),
    onSuccess: () => {
      setBroadcastOpen(false);
      setTitle("");
      setMessage("");
      success("Platform notification sent to all active users");
    },
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Admin", to: "/app/admin" }, { label: "Moderation & Inbox" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="font-display text-3xl font-bold">Marketplace Moderation & Communications</h1>
        <Button onClick={() => setBroadcastOpen(true)} icon={<Bell className="h-4 w-4" />}>
          Broadcast Platform Notification
        </Button>
      </div>

      <Card className="space-y-3">
        <h2 className="text-lg font-bold">Marketplace Products Moderation</h2>
        <div className="space-y-2.5">
          {(products?.items || []).map((p) => (
            <div key={p.id} className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[#DCE5DC] p-3 text-xs dark:border-[#203D31]">
              <div>
                <p className="font-bold">{p.name} — {formatCurrencyINR(p.priceInr)}</p>
                <p className="text-[#4B6358]">Supplier: {p.supplierName} • {p.categoryName}</p>
              </div>
              <div className="flex items-center gap-2">
                <Badge tone={p.approvalStatus === "APPROVED" ? "green" : p.approvalStatus === "REJECTED" ? "red" : "amber"}>
                  {p.approvalStatus}
                </Badge>
                <Button size="sm" variant="secondary" onClick={() => moderateProd.mutate({ id: p.id, status: "APPROVED" })} icon={<CheckCircle2 className="h-3.5 w-3.5" />}>
                  Approve
                </Button>
                <Button size="sm" variant="outline" onClick={() => moderateProd.mutate({ id: p.id, status: "REJECTED" })} icon={<XCircle className="h-3.5 w-3.5" />}>
                  Reject
                </Button>
              </div>
            </div>
          ))}
        </div>
      </Card>

      <Card className="space-y-3">
        <h2 className="text-lg font-bold">Farmer Produce Listings Moderation</h2>
        <div className="space-y-2.5">
          {(produce?.items || []).map((l) => (
            <div key={l.id} className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[#DCE5DC] p-3 text-xs dark:border-[#203D31]">
              <div>
                <p className="font-bold">{l.cropName} ({l.quantity} {l.unit})</p>
                <p className="text-[#4B6358]">Farmer: {l.farmerName} • {l.location}</p>
              </div>
              <div className="flex items-center gap-2">
                <Badge tone={l.moderationStatus === "APPROVED" ? "green" : "red"}>{l.moderationStatus}</Badge>
                <Button size="sm" variant="secondary" onClick={() => moderateProduce.mutate({ id: l.id, status: "APPROVED" })}>
                  Approve
                </Button>
                <Button size="sm" variant="outline" onClick={() => moderateProduce.mutate({ id: l.id, status: "FLAGGED" })}>
                  Flag
                </Button>
              </div>
            </div>
          ))}
        </div>
      </Card>

      <Card className="space-y-3">
        <h2 className="text-lg font-bold">Public Contact Inquiries</h2>
        <div className="space-y-2.5">
          {(contacts?.items || []).map((c) => (
            <div key={c.id} className="rounded-xl border border-[#DCE5DC] p-3 text-xs dark:border-[#203D31]">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <span className="font-bold">{c.subject} — {c.name} ({c.email})</span>
                <div className="flex items-center gap-2">
                  <Badge tone={c.status === "RESOLVED" ? "green" : "amber"}>{c.status}</Badge>
                  {c.status !== "RESOLVED" && (
                    <Button size="sm" variant="secondary" onClick={() => resolveContact.mutate({ id: c.id, status: "RESOLVED" })}>
                      Mark Resolved
                    </Button>
                  )}
                </div>
              </div>
              <p className="mt-1.5 text-[#4B6358] dark:text-[#9EB7AB]">{c.message}</p>
              <p className="mt-1 text-[11px] opacity-70">{formatDateTime(c.createdAt)}</p>
            </div>
          ))}
        </div>
      </Card>

      <Modal open={broadcastOpen} onClose={() => setBroadcastOpen(false)} title="Broadcast Notification to All Active Users">
        <div className="space-y-4">
          <Input label="Title" value={title} onChange={(e) => setTitle(e.target.value)} />
          <Textarea label="Message" value={message} onChange={(e) => setMessage(e.target.value)} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setBroadcastOpen(false)}>
              Cancel
            </Button>
            <Button loading={broadcast.isPending} disabled={!title || !message} onClick={() => broadcast.mutate()}>
              Send Broadcast
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
