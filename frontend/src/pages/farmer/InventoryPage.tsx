import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { AlertTriangle, ArrowDownUp, History, Package, Plus, Trash2 } from "lucide-react";
import { farmApi, inventoryApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage, formatCurrencyINR, formatDate } from "@/utils/format";
import type { InventoryItem } from "@/types/api";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { Drawer, Modal } from "@/components/ui/Modal";
import { Breadcrumbs, EmptyState } from "@/components/ui/Feedback";

const CATEGORIES = [
  "SEEDS",
  "FERTILIZERS",
  "PESTICIDES",
  "EQUIPMENT",
  "IRRIGATION_MATERIALS",
  "OTHER",
];

export function InventoryPage() {
  const queryClient = useQueryClient();
  const { success, error } = useToast();
  const [catFilter, setCatFilter] = useState("");
  const [itemModal, setItemModal] = useState(false);
  const [editing, setEditing] = useState<InventoryItem | null>(null);
  const [txTarget, setTxTarget] = useState<InventoryItem | null>(null);
  const [historyTarget, setHistoryTarget] = useState<InventoryItem | null>(null);

  const { data: farms } = useQuery({ queryKey: ["farms", "all"], queryFn: () => farmApi.list(undefined, 0, 50) });
  const { data: items } = useQuery({
    queryKey: ["inventory", catFilter],
    queryFn: () => inventoryApi.list(catFilter || undefined, 0, 50),
  });

  const { data: history } = useQuery({
    queryKey: ["inventory", "history", historyTarget?.id],
    queryFn: () => inventoryApi.history(historyTarget!.id),
    enabled: Boolean(historyTarget),
  });

  const itemForm = useForm({
    defaultValues: {
      farmId: "",
      name: "",
      category: "SEEDS",
      sku: "",
      quantity: 10,
      unit: "kg",
      reorderThreshold: 5,
      unitCostInr: 100,
      supplierName: "",
      notes: "",
    },
  });

  const txForm = useForm({
    defaultValues: {
      transactionType: "USAGE",
      quantity: 1,
      unitCostInr: "",
      referenceNote: "",
      transactionDate: new Date().toISOString().slice(0, 10),
    },
  });

  const openCreate = () => {
    setEditing(null);
    itemForm.reset({
      farmId: "",
      name: "",
      category: "SEEDS",
      sku: "",
      quantity: 10,
      unit: "kg",
      reorderThreshold: 5,
      unitCostInr: 100,
      supplierName: "",
      notes: "",
    });
    setItemModal(true);
  };

  const openEdit = (item: InventoryItem) => {
    setEditing(item);
    itemForm.reset({
      farmId: item.farmId || "",
      name: item.name,
      category: item.category,
      sku: item.sku || "",
      quantity: item.quantity,
      unit: item.unit,
      reorderThreshold: item.reorderThreshold,
      unitCostInr: item.unitCostInr,
      supplierName: item.supplierName || "",
      notes: item.notes || "",
    });
    setItemModal(true);
  };

  const saveItemMutation = useMutation({
    mutationFn: (v: Record<string, unknown>) => {
      const payload = {
        ...v,
        farmId: v.farmId || null,
        quantity: Number(v.quantity),
        reorderThreshold: Number(v.reorderThreshold),
        unitCostInr: Number(v.unitCostInr),
      };
      return editing ? inventoryApi.update(editing.id, payload) : inventoryApi.create(payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      queryClient.invalidateQueries({ queryKey: ["farmer", "dashboard"] });
      setItemModal(false);
      success(editing ? "Inventory item updated" : "Inventory item added");
    },
    onError: (err) => error("Failed to save item", extractErrorMessage(err)),
  });

  const txMutation = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      inventoryApi.recordTransaction(txTarget!.id, {
        ...v,
        quantity: Number(v.quantity),
        unitCostInr: v.unitCostInr ? Number(v.unitCostInr) : null,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      queryClient.invalidateQueries({ queryKey: ["farmer", "dashboard"] });
      setTxTarget(null);
      txForm.reset();
      success("Stock transaction recorded");
    },
    onError: (err) => error("Transaction failed", extractErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: inventoryApi.remove,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      success("Inventory item deleted");
    },
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Dashboard", to: "/app/farmer" }, { label: "Inventory & Resources" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Inventory & Resources</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Manage seeds, fertilizers, pesticides, equipment, irrigation supplies, and low-stock thresholds.
          </p>
        </div>
        <Button onClick={openCreate} icon={<Plus className="h-4 w-4" />}>
          Add Resource
        </Button>
      </div>

      <div className="flex flex-wrap gap-2">
        <button
          type="button"
          onClick={() => setCatFilter("")}
          className={`rounded-full px-3.5 py-1.5 text-xs font-semibold ${
            !catFilter ? "bg-[#10402E] text-white dark:bg-[#2A8C55]" : "bg-white dark:bg-[#0F211A]"
          }`}
        >
          All Categories
        </button>
        {CATEGORIES.map((c) => (
          <button
            key={c}
            type="button"
            onClick={() => setCatFilter(c)}
            className={`rounded-full px-3.5 py-1.5 text-xs font-semibold ${
              catFilter === c ? "bg-[#10402E] text-white dark:bg-[#2A8C55]" : "bg-white dark:bg-[#0F211A]"
            }`}
          >
            {c.replace(/_/g, " ")}
          </button>
        ))}
      </div>

      {!items || items.items.length === 0 ? (
        <EmptyState
          icon={<Package className="h-6 w-6" />}
          title="No inventory items found"
          description="Add your seeds, fertilizers, equipment, or irrigation materials to monitor stock levels."
        />
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {items.items.map((item) => (
            <Card key={item.id} className="flex flex-col justify-between space-y-3">
              <div>
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <h2 className="font-bold">{item.name}</h2>
                    <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">{item.category.replace(/_/g, " ")}</p>
                  </div>
                  {item.lowStock ? (
                    <Badge tone="red">
                      <AlertTriangle className="h-3 w-3" /> Low Stock
                    </Badge>
                  ) : (
                    <Badge tone="green">In Stock</Badge>
                  )}
                </div>

                <div className="mt-3 grid grid-cols-2 gap-2 rounded-xl bg-[#EEF3EE] p-3 text-xs dark:bg-[#152B22]">
                  <div>
                    <span className="text-[#4B6358] dark:text-[#9EB7AB]">Available</span>
                    <p className="text-base font-bold">
                      {item.quantity} {item.unit}
                    </p>
                  </div>
                  <div>
                    <span className="text-[#4B6358] dark:text-[#9EB7AB]">Unit Cost</span>
                    <p className="text-base font-bold">{formatCurrencyINR(item.unitCostInr)}</p>
                  </div>
                </div>
                <p className="mt-2 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                  Reorder alert at ≤ {item.reorderThreshold} {item.unit}
                  {item.supplierName ? ` • Supplier: ${item.supplierName}` : ""}
                </p>
              </div>

              <div className="flex flex-wrap items-center justify-between gap-2 border-t border-[#E9EFE9] pt-3 dark:border-[#1D392E]">
                <Button
                  size="sm"
                  variant="secondary"
                  onClick={() => {
                    setTxTarget(item);
                    txForm.setValue("unitCostInr", String(item.unitCostInr));
                  }}
                  icon={<ArrowDownUp className="h-3.5 w-3.5" />}
                >
                  Purchase / Use
                </Button>
                <div className="flex gap-1">
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => setHistoryTarget(item)}
                    icon={<History className="h-3.5 w-3.5" />}
                  >
                    History
                  </Button>
                  <Button size="sm" variant="ghost" onClick={() => openEdit(item)}>
                    Edit
                  </Button>
                  <Button
                    size="sm"
                    variant="ghost"
                    className="text-red-600"
                    onClick={() => deleteMutation.mutate(item.id)}
                    icon={<Trash2 className="h-3.5 w-3.5" />}
                  />
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Add/Edit Modal */}
      <Modal open={itemModal} onClose={() => setItemModal(false)} title={editing ? "Edit Resource" : "Add Resource"}>
        <form onSubmit={itemForm.handleSubmit((v) => saveItemMutation.mutate(v))} className="space-y-4">
          <Input label="Item Name" required {...itemForm.register("name", { required: true })} />
          <div className="grid gap-4 sm:grid-cols-2">
            <Select label="Category" {...itemForm.register("category")}>
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {c.replace(/_/g, " ")}
                </option>
              ))}
            </Select>
            <Select label="Farm (Optional)" {...itemForm.register("farmId")}>
              <option value="">All Farms / Central Store</option>
              {(farms?.items || []).map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </Select>
          </div>
          <div className="grid gap-4 sm:grid-cols-3">
            <Input label="Quantity" type="number" step="any" required {...itemForm.register("quantity")} />
            <Input label="Unit" required {...itemForm.register("unit", { required: true })} />
            <Input label="Low-Stock Alert At" type="number" step="any" {...itemForm.register("reorderThreshold")} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Unit Cost (INR ₹)" type="number" step="0.01" {...itemForm.register("unitCostInr")} />
            <Input label="Supplier Name" {...itemForm.register("supplierName")} />
          </div>
          <Textarea label="Notes" {...itemForm.register("notes")} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setItemModal(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={saveItemMutation.isPending}>
              Save Item
            </Button>
          </div>
        </form>
      </Modal>

      {/* Purchase / Usage Modal */}
      <Modal
        open={Boolean(txTarget)}
        onClose={() => setTxTarget(null)}
        title={`Record Stock Movement — ${txTarget?.name}`}
      >
        <form onSubmit={txForm.handleSubmit((v) => txMutation.mutate(v))} className="space-y-4">
          <Select label="Transaction Type" {...txForm.register("transactionType")}>
            <option value="USAGE">Field Usage (Deduct Stock)</option>
            <option value="PURCHASE">Purchase / Restock (Add Stock)</option>
          </Select>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label={`Quantity (${txTarget?.unit || "units"})`}
              type="number"
              step="0.01"
              required
              {...txForm.register("quantity", { required: true })}
            />
            <Input label="Date" type="date" required {...txForm.register("transactionDate", { required: true })} />
          </div>
          <Input label="Unit Cost (INR ₹, optional)" type="number" step="0.01" {...txForm.register("unitCostInr")} />
          <Input label="Reference Note" placeholder="e.g., Block A top dressing" {...txForm.register("referenceNote")} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setTxTarget(null)}>
              Cancel
            </Button>
            <Button type="submit" loading={txMutation.isPending}>
              Record Movement
            </Button>
          </div>
        </form>
      </Modal>

      {/* History Drawer */}
      <Drawer
        open={Boolean(historyTarget)}
        onClose={() => setHistoryTarget(null)}
        title={`Stock History: ${historyTarget?.name || ""}`}
      >
        <div className="space-y-3">
          {(history || []).map((tx) => (
            <div key={tx.id} className="rounded-xl border border-[#DCE5DC] p-3 text-xs dark:border-[#203D31]">
              <div className="flex items-center justify-between">
                <Badge tone={tx.quantityDelta >= 0 ? "green" : "amber"}>{tx.transactionType}</Badge>
                <span className="font-bold">
                  {tx.quantityDelta > 0 ? `+${tx.quantityDelta}` : tx.quantityDelta} {historyTarget?.unit}
                </span>
              </div>
              <p className="mt-1.5 text-[#4B6358] dark:text-[#9EB7AB]">
                {formatDate(tx.transactionDate)} • Cost: {formatCurrencyINR(tx.unitCostInr)}
              </p>
              {tx.referenceNote && <p className="mt-1">{tx.referenceNote}</p>}
            </div>
          ))}
          {(!history || history.length === 0) && (
            <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">No stock transactions logged yet.</p>
          )}
        </div>
      </Drawer>
    </div>
  );
}
