import { useState } from "react";
import { Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { ShoppingBag, Wheat } from "lucide-react";
import { marketplaceApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage, formatCurrencyINR, formatDate } from "@/utils/format";
import type { ProduceListing } from "@/types/api";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Breadcrumbs } from "@/components/ui/Feedback";

export function BuyerPortalPage() {
  const queryClient = useQueryClient();
  const { success, error } = useToast();
  const [search, setSearch] = useState("");
  const [inquiryTarget, setInquiryTarget] = useState<ProduceListing | null>(null);

  const { data: produce } = useQuery({
    queryKey: ["buyer", "produce", search],
    queryFn: () => marketplaceApi.listProduce(search || undefined, 0, 30),
  });
  const { data: myInquiries } = useQuery({ queryKey: ["buyer", "inquiries"], queryFn: marketplaceApi.myInquiries });

  const form = useForm({ defaultValues: { offeredPriceInr: "", requestedQuantity: 10, message: "" } });

  const sendInquiry = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      marketplaceApi.submitProduceInquiry(inquiryTarget!.id, {
        offeredPriceInr: v.offeredPriceInr ? Number(v.offeredPriceInr) : null,
        requestedQuantity: Number(v.requestedQuantity),
        message: v.message,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["buyer", "inquiries"] });
      setInquiryTarget(null);
      form.reset();
      success("Inquiry submitted");
    },
    onError: (err) => error("Failed to submit inquiry", extractErrorMessage(err)),
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Buyer & Procurement Portal" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Buyer & Market Procurement Portal</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Browse farmer produce lots, submit bulk inquiries, or order inputs from the marketplace.
          </p>
        </div>
        <Link to="/app/marketplace">
          <Button variant="secondary" icon={<ShoppingBag className="h-4 w-4" />}>
            Open Input Marketplace & Orders
          </Button>
        </Link>
      </div>

      <Input
        placeholder="Filter produce by crop name (e.g., Basmati, Tomato, Wheat)..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />

      <div className="grid gap-6 lg:grid-cols-12">
        <div className="grid gap-4 md:grid-cols-2 lg:col-span-8">
          {(produce?.items || []).map((l) => (
            <Card key={l.id} className="flex flex-col justify-between space-y-3">
              <div>
                <div className="flex items-start justify-between">
                  <div>
                    <h2 className="font-bold">
                      {l.cropName} {l.variety ? `(${l.variety})` : ""}
                    </h2>
                    <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                      {l.farmerName} • {l.location}
                    </p>
                  </div>
                  <Badge tone="green">{l.qualityGrade}</Badge>
                </div>
                <p className="mt-2 text-sm font-bold text-[#10402E] dark:text-[#56CF88]">
                  {formatCurrencyINR(l.expectedPriceInr)} / {l.unit.toLowerCase()} • {l.quantity} {l.unit}
                </p>
                {l.qualityNotes && <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">{l.qualityNotes}</p>}
              </div>
              <Button size="sm" onClick={() => setInquiryTarget(l)} icon={<Wheat className="h-4 w-4" />}>
                Submit Procurement Inquiry
              </Button>
            </Card>
          ))}
        </div>

        <Card className="h-fit space-y-3 lg:col-span-4">
          <h2 className="text-lg font-bold">My Submitted Inquiries</h2>
          {(myInquiries || []).map((inq) => (
            <div key={inq.id} className="rounded-xl border border-[#DCE5DC] p-3 text-xs dark:border-[#203D31]">
              <div className="flex items-center justify-between">
                <span className="font-bold">{inq.cropName}</span>
                <Badge tone={inq.status === "ACCEPTED" ? "green" : "amber"}>{inq.status}</Badge>
              </div>
              <p className="mt-1 text-[#4B6358] dark:text-[#9EB7AB]">
                Qty: {inq.requestedQuantity} • {formatDate(inq.createdAt)}
              </p>
              {inq.farmerReply && (
                <p className="mt-2 rounded-lg bg-[#EEF3EE] p-2 dark:bg-[#152B22]">
                  <strong>Farmer Reply:</strong> {inq.farmerReply}
                </p>
              )}
            </div>
          ))}
        </Card>
      </div>

      <Modal
        open={Boolean(inquiryTarget)}
        onClose={() => setInquiryTarget(null)}
        title={`Inquiry: ${inquiryTarget?.cropName || ""}`}
      >
        <form onSubmit={form.handleSubmit((v) => sendInquiry.mutate(v))} className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label={`Requested Quantity (${inquiryTarget?.unit || ""})`}
              type="number"
              step="0.01"
              required
              {...form.register("requestedQuantity", { required: true })}
            />
            <Input label="Offer Price (INR ₹)" type="number" step="0.01" {...form.register("offeredPriceInr")} />
          </div>
          <Textarea label="Message" required {...form.register("message", { required: true })} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setInquiryTarget(null)}>
              Cancel
            </Button>
            <Button type="submit" loading={sendInquiry.isPending}>
              Send Inquiry
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
