import { useState } from "react";
import { Download, FileSpreadsheet } from "lucide-react";
import { financeApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { downloadBlob, extractErrorMessage } from "@/utils/format";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Breadcrumbs } from "@/components/ui/Feedback";

const REPORT_TYPES = [
  { type: "CROPS", title: "Crop Reports", desc: "All planted crops, varieties, growth stages, and harvest status." },
  { type: "FARMS", title: "Farm Reports", desc: "Registered farms, acreage, soil type, irrigation setup, and district." },
  { type: "INVENTORY", title: "Inventory Reports", desc: "Seeds, fertilizers, equipment stock quantities, reorder status, and unit cost." },
  { type: "EXPENSES", title: "Expense Reports", desc: "Categorized INR farm expenses by date, farm, crop, and vendor." },
  { type: "HARVEST", title: "Harvest Reports", desc: "Estimated vs recorded actual yields across farm plots." },
  { type: "ACTIVITIES", title: "Activity Reports", desc: "Logged field cultivation stages, inputs applied, and operational costs." },
  { type: "FINANCE_PNL", title: "Profit & Loss Ledger", desc: "Combined income and expense audit ledger in INR (₹)." },
];

export function ReportsPage() {
  const { success, error } = useToast();
  const [busyKey, setBusyKey] = useState<string | null>(null);

  const handleDownload = async (type: string, format: "CSV" | "PDF") => {
    const key = `${type}-${format}`;
    setBusyKey(key);
    try {
      const blob = await financeApi.exportReport(type, format);
      downloadBlob(blob, `farmwise-${type.toLowerCase()}-report.${format.toLowerCase()}`);
      success(`Exported ${type} (${format})`);
    } catch (err) {
      error("Report download failed", extractErrorMessage(err));
    } finally {
      setBusyKey(null);
    }
  };

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Dashboard", to: "/app/farmer" }, { label: "Farm Reports" }]} />
      <div>
        <h1 className="font-display text-3xl font-bold">Farm Reports & Exports</h1>
        <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
          Generate clean CSV spreadsheets and formatted A4 PDF summaries directly from your PostgreSQL records.
        </p>
      </div>

      <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
        {REPORT_TYPES.map((r) => (
          <Card key={r.type} className="flex flex-col justify-between space-y-4">
            <div>
              <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-[#EEF3EE] text-[#10402E] dark:bg-[#173026] dark:text-[#4FD085]">
                <FileSpreadsheet className="h-5 w-5" />
              </div>
              <h2 className="text-lg font-bold">{r.title}</h2>
              <p className="mt-1 text-sm text-[#4B6358] dark:text-[#9EB7AB]">{r.desc}</p>
            </div>
            <div className="flex gap-2.5 pt-2">
              <Button
                size="sm"
                variant="outline"
                className="flex-1"
                loading={busyKey === `${r.type}-CSV`}
                onClick={() => handleDownload(r.type, "CSV")}
                icon={<Download className="h-4 w-4" />}
              >
                Export CSV
              </Button>
              <Button
                size="sm"
                className="flex-1"
                loading={busyKey === `${r.type}-PDF`}
                onClick={() => handleDownload(r.type, "PDF")}
                icon={<Download className="h-4 w-4" />}
              >
                Export PDF
              </Button>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}
