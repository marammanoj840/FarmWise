import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Activity, Shield } from "lucide-react";
import { adminApi } from "@/api/services";
import { formatDateTime } from "@/utils/format";
import { Badge, Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Field";
import { Breadcrumbs } from "@/components/ui/Feedback";

export function AdminAuditHealthPage() {
  const [actionFilter, setActionFilter] = useState("");
  const { data: logs } = useQuery({
    queryKey: ["admin", "audit-logs", actionFilter],
    queryFn: () => adminApi.auditLogs(actionFilter || undefined, 0, 50),
  });
  const { data: health } = useQuery({ queryKey: ["admin", "system-health"], queryFn: adminApi.systemHealth });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Admin", to: "/app/admin" }, { label: "Audit Logs & System Health" }]} />

      <h1 className="font-display text-3xl font-bold">Audit Logs & Integration Health</h1>

      {health && (
        <Card className="space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h2 className="inline-flex items-center gap-2 text-lg font-bold">
              <Activity className="h-5 w-5 text-[#2A8C55]" />
              Core Service & Integration Status
            </h2>
            <div className="flex gap-2">
              <Badge tone={health.coreHealth.status === "UP" ? "green" : "red"}>
                API: {health.coreHealth.status}
              </Badge>
              <Badge tone={health.coreHealth.databaseStatus === "UP" ? "green" : "red"}>
                PostgreSQL: {health.coreHealth.databaseStatus}
              </Badge>
            </div>
          </div>

          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            {health.integrations.map((intg) => (
              <div key={intg.integration} className="rounded-xl border border-[#DCE5DC] p-3 text-xs dark:border-[#203D31]">
                <div className="flex items-center justify-between">
                  <span className="font-bold">{intg.integration}</span>
                  <Badge tone={intg.configured ? "green" : "amber"}>
                    {intg.configured ? "Active" : "Awaiting Keys"}
                  </Badge>
                </div>
                <p className="mt-1 text-[#4B6358] dark:text-[#9EB7AB]">Provider: {intg.provider}</p>
                <p className="mt-0.5 text-[11px] opacity-80">{intg.statusNote}</p>
              </div>
            ))}
          </div>
        </Card>
      )}

      <Card className="space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h2 className="inline-flex items-center gap-2 text-lg font-bold">
            <Shield className="h-5 w-5 text-[#2A8C55]" />
            Security & Admin Audit Trail
          </h2>
          <div className="w-64">
            <Input
              placeholder="Filter action (e.g., ADMIN_, USER_)"
              value={actionFilter}
              onChange={(e) => setActionFilter(e.target.value)}
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-[#DCE5DC] text-left text-xs dark:divide-[#203D31]">
            <thead className="bg-[#EEF3EE] font-semibold uppercase dark:bg-[#132920]">
              <tr>
                <th className="px-3 py-2.5">Timestamp</th>
                <th className="px-3 py-2.5">Actor</th>
                <th className="px-3 py-2.5">Action</th>
                <th className="px-3 py-2.5">Target</th>
                <th className="px-3 py-2.5">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E9EFE9] dark:divide-[#1D392E]">
              {(logs?.items || []).map((l) => (
                <tr key={l.id}>
                  <td className="px-3 py-2.5 whitespace-nowrap">{formatDateTime(l.createdAt)}</td>
                  <td className="px-3 py-2.5">{l.actorEmail || "System"}</td>
                  <td className="px-3 py-2.5">
                    <Badge tone="slate">{l.action}</Badge>
                  </td>
                  <td className="px-3 py-2.5">
                    {l.entityType} {l.entityId ? `(${l.entityId.slice(0, 8)}…)` : ""}
                  </td>
                  <td className="px-3 py-2.5 font-mono text-[11px]">{l.detailsJson}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}
