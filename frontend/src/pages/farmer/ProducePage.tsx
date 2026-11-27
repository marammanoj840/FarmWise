import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { MessageSquare, Plus, Trash2, Wheat } from "lucide-react";
import { farmApi, marketplaceApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage, formatCurrencyINR, formatDate } from "@/utils/format";
import type { ProduceInquiry, ProduceListing } from "@/types/api";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { Drawer, Modal } from "@/components/ui/Modal";
import { Breadcrumbs, EmptyState } from "@/components/ui/Feedback";

export function ProducePage() {
  const queryClient = useQueryClient();
  const { success, error } = useToast();
  const [modalOpen, setModalOpen] = useState(false);
  const [inquiryListing, setInquiryListing] = useState<ProduceListing | null>(null);
  const [replyText, setReplyText] = useState<Record<string, string>>({});

  const { data: farms } = useQuery({ queryKey: ["farms", "all"], queryFn: () => farmApi.list(undefined, 0, 50) });
  const { data: listings } = useQuery({ queryKey: ["produce", "mine"], queryFn: marketplaceApi.myProduce });
  const { data: inquiries } = useQuery({
    queryKey: ["produce", "inquiries", inquiryListing?.id],
    queryFn: () => marketplaceApi.listingInquiries(inquiryListing!.id),
    enabled: Boolean(inquiryListing),
  });

  const form = useForm({
    defaultValues: {
      farmId: "",
      cropName: "",
      variety: "",
      quantity: 20,
      unit: "QUINTAL",
      expectedPriceInr: 2400,
      location: "",
      qualityGrade: "Grade A",
      qualityNotes: "",
      harvestDate: new Date().toISOString().slice(0, 10),
    },
  });

  const createMutation = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      marketplaceApi.createProduce({
        ...v,
        farmId: v.farmId || null,
        quantity: Number(v.quantity),
        expectedPriceInr: Number(v.expectedPriceInr),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["produce"] });
      setModalOpen(false);
      form.reset();
      success("Produce listing published");
    },
    onError: (err) => error("Failed to publish listing", extractErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: marketplaceApi.deleteProduce,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["produce"] });
      success("Listing deleted");
    },
  });

  const replyMutation = useMutation({
    mutationFn: ({ inq, status }: { inq: ProduceInquiry; status: string }) =>
      marketplaceApi.replyInquiry(inq.id, {
        status,
        farmerReply: replyText[inq.id] || `Listing inquiry marked ${status}.`,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["produce", "inquiries", inquiryListing?.id] });
      success("Reply sent to buyer");
    },
    onError: (err) => error("Reply failed", extractErrorMessage(err)),
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Dashboard", to: "/app/farmer" }, { label: "My Produce Listings" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Farmer Produce Marketplace</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            List harvested or upcoming crop lots directly for buyers and manage price inquiries.
          </p>
        </div>
        <Button onClick={() => setModalOpen(true)} icon={<Plus className="h-4 w-4" />}>
          Create Produce Listing
        </Button>
      </div>

      {!listings || listings.length === 0 ? (
        <EmptyState
          icon={<Wheat className="h-6 w-6" />}
          title="No produce listings published"
          description="List your harvest quantity, quality grade, and expected INR price for buyers."
        />
      ) : (
        <div className="grid gap-5 md:grid-cols-2">
          {listings.map((l) => (
            <Card key={l.id} className="space-y-3">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <h2 className="text-lg font-bold">
                    {l.cropName} {l.variety ? `(${l.variety})` : ""}
                  </h2>
                  <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">{l.location}</p>
                </div>
                <div className="flex gap-1.5">
                  <Badge tone="green">{l.listingStatus}</Badge>
                  <Badge tone="slate">{l.qualityGrade}</Badge>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-2 rounded-xl bg-[#EEF3EE] p-3 text-xs dark:bg-[#152B22]">
                <div>
                  <span className="text-[#4B6358]">Quantity</span>
                  <p className="font-bold">
                    {l.quantity} {l.unit}
                  </p>
                </div>
                <div>
                  <span className="text-[#4B6358]">Expected Price</span>
                  <p className="font-bold">
                    {formatCurrencyINR(l.expectedPriceInr)}/{l.unit.toLowerCase()}
                  </p>
                </div>
                <div>
                  <span className="text-[#4B6358]">Harvest Date</span>
                  <p className="font-bold">{formatDate(l.harvestDate)}</p>
                </div>
              </div>

              {l.qualityNotes && <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">{l.qualityNotes}</p>}

              <div className="flex justify-between border-t border-[#E9EFE9] pt-3 dark:border-[#1D392E]">
                <Button
                  size="sm"
                  variant="secondary"
                  onClick={() => setInquiryListing(l)}
                  icon={<MessageSquare className="h-3.5 w-3.5" />}
                >
                  View Buyer Inquiries
                </Button>
                <Button
                  size="sm"
                  variant="ghost"
                  className="text-red-600"
                  onClick={() => deleteMutation.mutate(l.id)}
                  icon={<Trash2 className="h-3.5 w-3.5" />}
                />
              </div>
            </Card>
          ))}
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Publish Produce Listing">
        <form onSubmit={form.handleSubmit((v) => createMutation.mutate(v))} className="space-y-4">
          <Select label="Source Farm (Optional)" {...form.register("farmId")}>
            <option value="">Select farm...</option>
            {(farms?.items || []).map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </Select>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Crop Name" required {...form.register("cropName", { required: true })} />
            <Input label="Variety" {...form.register("variety")} />
          </div>
          <div className="grid gap-4 sm:grid-cols-3">
            <Input label="Quantity" type="number" step="0.01" required {...form.register("quantity", { required: true })} />
            <Select label="Unit" {...form.register("unit")}>
              <option value="QUINTAL">Quintal (100 kg)</option>
              <option value="KG">Kilogram (kg)</option>
              <option value="TONNE">Metric Tonne</option>
            </Select>
            <Input
              label="Expected Price (INR ₹)"
              type="number"
              step="0.01"
              required
              {...form.register("expectedPriceInr", { required: true })}
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Pickup Location" required {...form.register("location", { required: true })} />
            <Input label="Quality Grade" required {...form.register("qualityGrade", { required: true })} />
          </div>
          <Input label="Harvest Date" type="date" {...form.register("harvestDate")} />
          <Textarea label="Quality & Storage Notes" {...form.register("qualityNotes")} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={createMutation.isPending}>
              Publish Listing
            </Button>
          </div>
        </form>
      </Modal>

      <Drawer
        open={Boolean(inquiryListing)}
        onClose={() => setInquiryListing(null)}
        title={`Buyer Inquiries: ${inquiryListing?.cropName || ""}`}
      >
        <div className="space-y-4">
          {(inquiries || []).map((inq) => (
            <Card key={inq.id} padding="p-4" className="space-y-2.5 text-xs">
              <div className="flex items-center justify-between">
                <span className="font-bold">{inq.buyerName}</span>
                <Badge tone={inq.status === "ACCEPTED" ? "green" : "amber"}>{inq.status}</Badge>
              </div>
              <p>
                Requested: <strong>{inq.requestedQuantity}</strong> • Offered:{" "}
                <strong>{inq.offeredPriceInr ? formatCurrencyINR(inq.offeredPriceInr) : "Ask price"}</strong>
              </p>
              <p className="text-[#4B6358] dark:text-[#9EB7AB]">{inq.message}</p>
              {inq.farmerReply ? (
                <p className="rounded-lg bg-[#EEF3EE] p-2 dark:bg-[#152B22]">
                  <strong>Your Reply:</strong> {inq.farmerReply}
                </p>
              ) : (
                <div className="space-y-2 pt-2">
                  <input
                    type="text"
                    placeholder="Write response to buyer..."
                    value={replyText[inq.id] || ""}
                    onChange={(e) => setReplyText((prev) => ({ ...prev, [inq.id]: e.target.value }))}
                    className="min-h-9 w-full rounded-lg border border-[#C9D6CA] px-3 text-xs dark:border-[#244337] dark:bg-[#0D1E17]"
                  />
                  <div className="flex gap-2">
                    <Button
                      size="sm"
                      className="flex-1"
                      onClick={() => replyMutation.mutate({ inq, status: "ACCEPTED" })}
                    >
                      Accept / Discuss
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      className="flex-1"
                      onClick={() => replyMutation.mutate({ inq, status: "DECLINED" })}
                    >
                      Decline
                    </Button>
                  </div>
                </div>
              )}
            </Card>
          ))}
          {(!inquiries || inquiries.length === 0) && (
            <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">No buyer inquiries received yet.</p>
          )}
        </div>
      </Drawer>
    </div>
  );
}
