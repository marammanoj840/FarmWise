import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { AlertCircle, Download, Plus, Trash2 } from "lucide-react";
import { cropApi, farmApi, financeApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { downloadBlob, extractErrorMessage, formatCurrencyINR, formatDate } from "@/utils/format";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Breadcrumbs } from "@/components/ui/Feedback";

export function FinancePage() {
  const queryClient = useQueryClient();
  const { success, error } = useToast();
  const [expenseModal, setExpenseModal] = useState(false);
  const [incomeModal, setIncomeModal] = useState(false);

  const { data: summary } = useQuery({ queryKey: ["finance", "summary"], queryFn: financeApi.summary });
  const { data: expenses } = useQuery({ queryKey: ["finance", "expenses"], queryFn: () => financeApi.listExpenses(0, 50) });
  const { data: income } = useQuery({ queryKey: ["finance", "income"], queryFn: () => financeApi.listIncome(0, 50) });
  const { data: farms } = useQuery({ queryKey: ["farms", "all"], queryFn: () => farmApi.list(undefined, 0, 50) });
  const { data: crops } = useQuery({ queryKey: ["crops", "all"], queryFn: () => cropApi.list(undefined, 0, 50) });

  const expForm = useForm({
    defaultValues: {
      farmId: "",
      farmCropId: "",
      category: "SEEDS",
      title: "",
      amountInr: "",
      expenseDate: new Date().toISOString().slice(0, 10),
      vendorName: "",
      paymentMethod: "UPI",
      notes: "",
    },
  });

  const incForm = useForm({
    defaultValues: {
      farmId: "",
      farmCropId: "",
      category: "CROP_SALE",
      title: "",
      amountInr: "",
      incomeDate: new Date().toISOString().slice(0, 10),
      buyerName: "",
      quantitySold: "",
      unit: "QUINTAL",
      notes: "",
    },
  });

  const createExp = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      financeApi.createExpense({
        ...v,
        farmId: v.farmId || null,
        farmCropId: v.farmCropId || null,
        amountInr: Number(v.amountInr),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["finance"] });
      queryClient.invalidateQueries({ queryKey: ["farmer", "dashboard"] });
      setExpenseModal(false);
      expForm.reset();
      success("Expense recorded");
    },
    onError: (err) => error("Failed to save expense", extractErrorMessage(err)),
  });

  const createInc = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      financeApi.createIncome({
        ...v,
        farmId: v.farmId || null,
        farmCropId: v.farmCropId || null,
        amountInr: Number(v.amountInr),
        quantitySold: v.quantitySold ? Number(v.quantitySold) : null,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["finance"] });
      queryClient.invalidateQueries({ queryKey: ["farmer", "dashboard"] });
      setIncomeModal(false);
      incForm.reset();
      success("Income recorded");
    },
    onError: (err) => error("Failed to save income", extractErrorMessage(err)),
  });

  const deleteExp = useMutation({
    mutationFn: financeApi.deleteExpense,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["finance"] }),
  });

  const deleteInc = useMutation({
    mutationFn: financeApi.deleteIncome,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["finance"] }),
  });

  const handleExport = async (format: "CSV" | "PDF") => {
    try {
      const blob = await financeApi.exportReport("FINANCE_PNL", format);
      downloadBlob(blob, `farmwise-finance-pnl.${format.toLowerCase()}`);
      success(`Downloaded ${format} report`);
    } catch (err) {
      error("Export failed", extractErrorMessage(err));
    }
  };

  const chartData = (summary?.byFarm || []).map((f) => ({
    name: f.farmName.length > 18 ? f.farmName.slice(0, 18) + "…" : f.farmName,
    Income: Number(f.totalIncomeInr),
    Expenses: Number(f.totalExpensesInr),
  }));

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Dashboard", to: "/app/farmer" }, { label: "Expenses & Finance" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Farm Expenses & Finance (INR ₹)</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Track crop-wise expenses, harvest revenues, and farm P&L statements.
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button size="sm" variant="outline" onClick={() => handleExport("CSV")} icon={<Download className="h-4 w-4" />}>
            CSV
          </Button>
          <Button size="sm" variant="outline" onClick={() => handleExport("PDF")} icon={<Download className="h-4 w-4" />}>
            PDF
          </Button>
          <Button size="sm" variant="secondary" onClick={() => setIncomeModal(true)} icon={<Plus className="h-4 w-4" />}>
            Add Income
          </Button>
          <Button size="sm" onClick={() => setExpenseModal(true)} icon={<Plus className="h-4 w-4" />}>
            Add Expense
          </Button>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid gap-4 sm:grid-cols-3">
        <Card>
          <p className="text-xs font-semibold uppercase text-[#4B6358]">Recorded Income</p>
          <p className="mt-2 font-display text-3xl font-bold text-emerald-700 dark:text-emerald-400">
            {formatCurrencyINR(summary?.totalIncomeInr)}
          </p>
        </Card>
        <Card>
          <p className="text-xs font-semibold uppercase text-[#4B6358]">Recorded Expenses</p>
          <p className="mt-2 font-display text-3xl font-bold text-red-700 dark:text-red-400">
            {formatCurrencyINR(summary?.totalExpensesInr)}
          </p>
        </Card>
        <Card>
          <p className="text-xs font-semibold uppercase text-[#4B6358]">Net Profit / Loss</p>
          <p className="mt-2 font-display text-3xl font-bold">{formatCurrencyINR(summary?.netProfitLossInr)}</p>
        </Card>
      </div>

      {summary?.disclaimer && (
        <div className="flex items-start gap-2.5 rounded-xl border border-amber-200 bg-amber-50/70 p-3.5 text-xs text-amber-950 dark:border-amber-900/60 dark:bg-amber-950/30 dark:text-amber-200">
          <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
          <span>{summary.disclaimer}</span>
        </div>
      )}

      {/* Farm P&L Chart & Crop Breakdown */}
      <div className="grid gap-6 lg:grid-cols-12">
        <Card className="lg:col-span-7">
          <h2 className="mb-4 text-lg font-bold">Farm-Wise Income vs Expenses (INR)</h2>
          {chartData.length > 0 ? (
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" opacity={0.25} />
                  <XAxis dataKey="name" fontSize={12} />
                  <YAxis fontSize={12} />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="Income" fill="#2A8C55" radius={[6, 6, 0, 0]} />
                  <Bar dataKey="Expenses" fill="#D97706" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <p className="text-sm text-[#4B6358]">Add farms and transactions to visualize farm-level P&L.</p>
          )}
        </Card>

        <Card className="lg:col-span-5">
          <h2 className="mb-3 text-lg font-bold">Crop-Wise Net Summary</h2>
          <div className="space-y-2.5">
            {(summary?.byCrop || []).map((c) => (
              <div key={c.farmCropId} className="flex items-center justify-between rounded-xl bg-[#EEF3EE] p-3 text-xs dark:bg-[#152B22]">
                <div>
                  <p className="font-bold">{c.cropName}</p>
                  <p className="text-[#4B6358] dark:text-[#9EB7AB]">
                    In: {formatCurrencyINR(c.totalIncomeInr)} • Out: {formatCurrencyINR(c.totalExpensesInr)}
                  </p>
                </div>
                <Badge tone={Number(c.netBalanceInr) >= 0 ? "green" : "red"}>
                  {formatCurrencyINR(c.netBalanceInr)}
                </Badge>
              </div>
            ))}
            {(!summary?.byCrop || summary.byCrop.length === 0) && (
              <p className="text-sm text-[#4B6358]">No crop-linked financial records yet.</p>
            )}
          </div>
        </Card>
      </div>

      {/* Transaction Lists */}
      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <h2 className="mb-3 text-lg font-bold">Expense Ledger</h2>
          <div className="space-y-2.5">
            {(expenses?.items || []).map((e) => (
              <div key={e.id} className="flex items-center justify-between border-b border-[#E9EFE9] pb-2.5 last:border-0 dark:border-[#1D392E]">
                <div>
                  <p className="text-sm font-semibold">{e.title}</p>
                  <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                    {e.category} • {formatDate(e.expenseDate)} {e.farmName ? `• ${e.farmName}` : ""}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-sm font-bold text-red-700 dark:text-red-400">
                    -{formatCurrencyINR(e.amountInr)}
                  </span>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => deleteExp.mutate(e.id)}
                    icon={<Trash2 className="h-3.5 w-3.5 text-red-600" />}
                  />
                </div>
              </div>
            ))}
          </div>
        </Card>

        <Card>
          <h2 className="mb-3 text-lg font-bold">Income Ledger</h2>
          <div className="space-y-2.5">
            {(income?.items || []).map((i) => (
              <div key={i.id} className="flex items-center justify-between border-b border-[#E9EFE9] pb-2.5 last:border-0 dark:border-[#1D392E]">
                <div>
                  <p className="text-sm font-semibold">{i.title}</p>
                  <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                    {i.category} • {formatDate(i.incomeDate)} {i.buyerName ? `• ${i.buyerName}` : ""}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-sm font-bold text-emerald-700 dark:text-emerald-400">
                    +{formatCurrencyINR(i.amountInr)}
                  </span>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => deleteInc.mutate(i.id)}
                    icon={<Trash2 className="h-3.5 w-3.5 text-red-600" />}
                  />
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>

      {/* Expense Modal */}
      <Modal open={expenseModal} onClose={() => setExpenseModal(false)} title="Record Farm Expense (INR)">
        <form onSubmit={expForm.handleSubmit((v) => createExp.mutate(v))} className="space-y-4">
          <Input label="Expense Title" required {...expForm.register("title", { required: true })} />
          <div className="grid gap-4 sm:grid-cols-2">
            <Select label="Category" {...expForm.register("category")}>
              <option value="SEEDS">Seeds</option>
              <option value="FERTILIZERS">Fertilizers</option>
              <option value="PESTICIDES">Pesticides</option>
              <option value="LABOR">Labor</option>
              <option value="IRRIGATION">Irrigation & Power</option>
              <option value="EQUIPMENT">Equipment / Fuel</option>
              <option value="OTHER">Other</option>
            </Select>
            <Input label="Amount (INR ₹)" type="number" step="0.01" required {...expForm.register("amountInr", { required: true })} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Select label="Farm (Optional)" {...expForm.register("farmId")}>
              <option value="">General</option>
              {(farms?.items || []).map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </Select>
            <Select label="Crop (Optional)" {...expForm.register("farmCropId")}>
              <option value="">General</option>
              {(crops?.items || []).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.cropName}
                </option>
              ))}
            </Select>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Date" type="date" required {...expForm.register("expenseDate", { required: true })} />
            <Input label="Vendor Name" {...expForm.register("vendorName")} />
          </div>
          <Textarea label="Notes" {...expForm.register("notes")} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setExpenseModal(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={createExp.isPending}>
              Save Expense
            </Button>
          </div>
        </form>
      </Modal>

      {/* Income Modal */}
      <Modal open={incomeModal} onClose={() => setIncomeModal(false)} title="Record Farm Income (INR)">
        <form onSubmit={incForm.handleSubmit((v) => createInc.mutate(v))} className="space-y-4">
          <Input label="Income Title" required {...incForm.register("title", { required: true })} />
          <div className="grid gap-4 sm:grid-cols-2">
            <Select label="Category" {...incForm.register("category")}>
              <option value="CROP_SALE">Crop Sale</option>
              <option value="PRODUCE_DIRECT">Direct Produce Sale</option>
              <option value="SUBSIDY">Government Grant / Subsidy</option>
              <option value="BYPRODUCT">By-product / Fodder</option>
              <option value="OTHER">Other</option>
            </Select>
            <Input label="Amount (INR ₹)" type="number" step="0.01" required {...incForm.register("amountInr", { required: true })} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Select label="Farm (Optional)" {...incForm.register("farmId")}>
              <option value="">General</option>
              {(farms?.items || []).map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </Select>
            <Select label="Crop (Optional)" {...incForm.register("farmCropId")}>
              <option value="">General</option>
              {(crops?.items || []).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.cropName}
                </option>
              ))}
            </Select>
          </div>
          <div className="grid gap-4 sm:grid-cols-3">
            <Input label="Date" type="date" required {...incForm.register("incomeDate", { required: true })} />
            <Input label="Quantity Sold" type="number" step="any" {...incForm.register("quantitySold")} />
            <Input label="Buyer Name" {...incForm.register("buyerName")} />
          </div>
          <Textarea label="Notes" {...incForm.register("notes")} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setIncomeModal(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={createInc.isPending}>
              Save Income
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
