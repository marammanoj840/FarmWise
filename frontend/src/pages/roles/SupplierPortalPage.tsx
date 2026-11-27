import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Package, Plus, Store } from "lucide-react";
import { marketplaceApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage, formatCurrencyINR } from "@/utils/format";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Breadcrumbs } from "@/components/ui/Feedback";

export function SupplierPortalPage() {
  const queryClient = useQueryClient();
  const { success, error } = useToast();
  const [prodModal, setProdModal] = useState(false);

  const { data: profile } = useQuery({ queryKey: ["supplier", "profile"], queryFn: marketplaceApi.supplierProfile });
  const { data: products } = useQuery({ queryKey: ["supplier", "products"], queryFn: marketplaceApi.supplierProducts });
  const { data: orders } = useQuery({ queryKey: ["supplier", "orders"], queryFn: marketplaceApi.supplierOrders });
  const { data: categories } = useQuery({ queryKey: ["marketplace", "categories"], queryFn: marketplaceApi.categories });

  const profForm = useForm({
    defaultValues: { businessName: "", gstin: "", description: "", city: "", state: "", phone: "" },
  });

  useEffect(() => {
    if (profile) {
      profForm.reset({
        businessName: profile.businessName || "",
        gstin: profile.gstin || "",
        description: profile.description || "",
        city: profile.city || "",
        state: profile.state || "",
        phone: profile.phone || "",
      });
    }
  }, [profile, profForm]);

  const prodForm = useForm({
    defaultValues: {
      categoryId: "",
      name: "",
      description: "",
      unit: "bag",
      priceInr: 450,
      stockQuantity: 25,
      brand: "",
      imageUrl: "",
    },
  });

  const saveProfile = useMutation({
    mutationFn: marketplaceApi.updateSupplierProfile,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["supplier", "profile"] });
      success("Storefront profile updated");
    },
    onError: (err) => error("Update failed", extractErrorMessage(err)),
  });

  const createProduct = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      marketplaceApi.createProduct({
        ...v,
        priceInr: Number(v.priceInr),
        stockQuantity: Number(v.stockQuantity),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["supplier", "products"] });
      setProdModal(false);
      prodForm.reset();
      success("Product published");
    },
    onError: (err) => error("Failed to create product", extractErrorMessage(err)),
  });

  const updateFulfillment = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      marketplaceApi.updateOrderItemStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["supplier", "orders"] });
      success("Fulfillment status updated");
    },
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Supplier Portal" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Agricultural Supplier Portal</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Manage agricultural input products, stock levels, and incoming farmer/buyer orders.
          </p>
        </div>
        <div className="flex items-center gap-2">
          {profile && (
            <Badge tone={profile.verificationStatus === "APPROVED" ? "green" : "amber"}>
              Verification: {profile.verificationStatus}
            </Badge>
          )}
          <Button onClick={() => setProdModal(true)} icon={<Plus className="h-4 w-4" />}>
            Add Product
          </Button>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-12">
        <div className="space-y-6 lg:col-span-8">
          <Card className="space-y-3">
            <h2 className="inline-flex items-center gap-2 text-lg font-bold">
              <Package className="h-5 w-5 text-[#2A8C55]" />
              My Products ({products?.length ?? 0})
            </h2>
            <div className="grid gap-3 sm:grid-cols-2">
              {(products || []).map((p) => (
                <div key={p.id} className="rounded-xl border border-[#DCE5DC] p-3.5 text-xs dark:border-[#203D31]">
                  <div className="flex items-center justify-between gap-2">
                    <span className="font-bold">{p.name}</span>
                    <Badge tone={p.approvalStatus === "APPROVED" ? "green" : "amber"}>{p.approvalStatus}</Badge>
                  </div>
                  <p className="mt-1 text-[#4B6358] dark:text-[#9EB7AB]">
                    {p.categoryName} • {formatCurrencyINR(p.priceInr)}/{p.unit} • Stock: {p.stockQuantity}
                  </p>
                </div>
              ))}
            </div>
          </Card>

          <Card className="space-y-3">
            <h2 className="text-lg font-bold">Incoming Order Line Items ({orders?.length ?? 0})</h2>
            {(orders || []).map((oi) => (
              <div key={oi.id} className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-[#DCE5DC] p-3 text-xs dark:border-[#203D31]">
                <div>
                  <p className="font-bold">
                    {oi.productName} × {oi.quantity}
                  </p>
                  <p className="text-[#4B6358]">Line Total: {formatCurrencyINR(oi.lineTotalInr)}</p>
                </div>
                <select
                  value={oi.fulfillmentStatus}
                  onChange={(e) => updateFulfillment.mutate({ id: oi.id, status: e.target.value })}
                  className="rounded-lg border border-[#C9D6CA] bg-white px-2.5 py-1.5 text-xs font-semibold dark:border-[#244337] dark:bg-[#0D1E17]"
                >
                  <option value="PENDING">PENDING</option>
                  <option value="PACKED">PACKED</option>
                  <option value="SHIPPED">SHIPPED</option>
                  <option value="DELIVERED">DELIVERED</option>
                </select>
              </div>
            ))}
          </Card>
        </div>

        <Card className="h-fit space-y-4 lg:col-span-4">
          <h2 className="inline-flex items-center gap-2 text-lg font-bold">
            <Store className="h-5 w-5 text-[#2A8C55]" />
            Storefront Profile
          </h2>
          <form onSubmit={profForm.handleSubmit((v) => saveProfile.mutate(v))} className="space-y-3">
            <Input label="Business Name" required {...profForm.register("businessName", { required: true })} />
            <Input label="GSTIN (Optional)" {...profForm.register("gstin")} />
            <div className="grid grid-cols-2 gap-3">
              <Input label="City" {...profForm.register("city")} />
              <Input label="State" {...profForm.register("state")} />
            </div>
            <Input label="Phone" {...profForm.register("phone")} />
            <Textarea label="Description" {...profForm.register("description")} />
            <Button type="submit" loading={saveProfile.isPending} className="w-full">
              Save Business Profile
            </Button>
          </form>
        </Card>
      </div>

      <Modal open={prodModal} onClose={() => setProdModal(false)} title="Add Marketplace Product">
        <form onSubmit={prodForm.handleSubmit((v) => createProduct.mutate(v))} className="space-y-4">
          <Select label="Category" required {...prodForm.register("categoryId", { required: true })}>
            <option value="">Select category...</option>
            {(categories || []).map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </Select>
          <Input label="Product Name" required {...prodForm.register("name", { required: true })} />
          <div className="grid gap-4 sm:grid-cols-3">
            <Input label="Price (INR ₹)" type="number" step="0.01" required {...prodForm.register("priceInr", { required: true })} />
            <Input label="Unit" required {...prodForm.register("unit", { required: true })} />
            <Input label="Stock Qty" type="number" required {...prodForm.register("stockQuantity", { required: true })} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Brand" {...prodForm.register("brand")} />
            <Input label="Image URL (Optional)" {...prodForm.register("imageUrl")} />
          </div>
          <Textarea label="Description" required {...prodForm.register("description", { required: true })} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setProdModal(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={createProduct.isPending}>
              Publish Product
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
