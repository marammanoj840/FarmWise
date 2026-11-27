import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CheckCircle2, ShieldAlert, UserCheck, UserX } from "lucide-react";
import { adminApi, expertApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage } from "@/utils/format";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Select } from "@/components/ui/Field";
import { Breadcrumbs } from "@/components/ui/Feedback";

export function AdminUsersPage() {
  const queryClient = useQueryClient();
  const { success, error } = useToast();
  const [roleFilter, setRoleFilter] = useState("");
  const [tab, setTab] = useState<"users" | "experts" | "suppliers">("users");

  const { data: users } = useQuery({
    queryKey: ["admin", "users", roleFilter],
    queryFn: () => adminApi.users(roleFilter || undefined, 0, 50),
  });
  const { data: experts } = useQuery({ queryKey: ["admin", "experts"], queryFn: () => expertApi.list() });
  const { data: suppliers } = useQuery({ queryKey: ["admin", "suppliers"], queryFn: adminApi.suppliers });

  const toggleStatus = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) => adminApi.updateUserStatus(id, active),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      success("User status updated");
    },
    onError: (err) => error("Failed", extractErrorMessage(err)),
  });

  const changeRole = useMutation({
    mutationFn: ({ id, role }: { id: string; role: string }) => adminApi.updateUserRole(id, role),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      success("User role updated");
    },
    onError: (err) => error("Failed", extractErrorMessage(err)),
  });

  const verifyExpert = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => adminApi.verifyExpert(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "experts"] });
      queryClient.invalidateQueries({ queryKey: ["experts"] });
      success("Expert verification updated");
    },
  });

  const verifySupplier = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => adminApi.verifySupplier(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "suppliers"] });
      success("Supplier verification updated");
    },
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Admin", to: "/app/admin" }, { label: "Users & Verifications" }]} />

      <h1 className="font-display text-3xl font-bold">Users, Roles & Stakeholder Verification</h1>

      <div className="flex flex-wrap gap-2 border-b border-[#DCE5DC] pb-2 dark:border-[#203D31]">
        {[
          { id: "users", label: "All Users & Roles" },
          { id: "experts", label: `Expert Verifications (${experts?.length ?? 0})` },
          { id: "suppliers", label: `Supplier Verifications (${suppliers?.length ?? 0})` },
        ].map((t) => (
          <button
            key={t.id}
            type="button"
            onClick={() => setTab(t.id as typeof tab)}
            className={`rounded-xl px-4 py-2 text-sm font-semibold ${
              tab === t.id ? "bg-[#10402E] text-white dark:bg-[#2A8C55]" : "text-[#4B6358] hover:bg-[#EEF3EE]"
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === "users" && (
        <div className="space-y-4">
          <div className="max-w-xs">
            <Select label="Filter by Role" value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)}>
              <option value="">All Roles</option>
              <option value="FARMER">FARMER</option>
              <option value="EXPERT">EXPERT</option>
              <option value="SUPPLIER">SUPPLIER</option>
              <option value="BUYER">BUYER</option>
              <option value="ADMIN">ADMIN</option>
            </Select>
          </div>
          <Card padding="p-0" className="overflow-x-auto">
            <table className="min-w-full divide-y divide-[#DCE5DC] text-left text-sm dark:divide-[#203D31]">
              <thead className="bg-[#EEF3EE] text-xs font-semibold uppercase dark:bg-[#132920]">
                <tr>
                  <th className="px-4 py-3">User</th>
                  <th className="px-4 py-3">Role</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#E9EFE9] dark:divide-[#1D392E]">
                {(users?.items || []).map((u) => (
                  <tr key={u.id}>
                    <td className="px-4 py-3">
                      <p className="font-bold">{u.fullName}</p>
                      <p className="text-xs text-[#4B6358]">{u.email}</p>
                    </td>
                    <td className="px-4 py-3">
                      <select
                        value={u.primaryRole}
                        onChange={(e) => changeRole.mutate({ id: u.id, role: e.target.value })}
                        className="rounded-lg border border-[#C9D6CA] bg-white px-2.5 py-1 text-xs font-semibold dark:border-[#244337] dark:bg-[#0D1E17]"
                      >
                        {["FARMER", "EXPERT", "SUPPLIER", "BUYER", "ADMIN"].map((r) => (
                          <option key={r} value={r}>
                            {r}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td className="px-4 py-3">
                      <Badge tone={u.active ? "green" : "red"}>{u.active ? "Active" : "Deactivated"}</Badge>
                    </td>
                    <td className="px-4 py-3">
                      <Button
                        size="sm"
                        variant={u.active ? "outline" : "secondary"}
                        onClick={() => toggleStatus.mutate({ id: u.id, active: !u.active })}
                        icon={u.active ? <UserX className="h-3.5 w-3.5" /> : <UserCheck className="h-3.5 w-3.5" />}
                      >
                        {u.active ? "Deactivate" : "Activate"}
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </Card>
        </div>
      )}

      {tab === "experts" && (
        <div className="grid gap-4 md:grid-cols-2">
          {(experts || []).map((e) => (
            <Card key={e.id} className="space-y-3">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="font-bold">{e.fullName}</h3>
                  <p className="text-xs text-[#4B6358]">{e.headline} • {e.specializations}</p>
                </div>
                <Badge tone={e.verified ? "green" : "amber"}>
                  {e.verified ? <CheckCircle2 className="h-3 w-3" /> : <ShieldAlert className="h-3 w-3" />}
                  {e.verificationStatus}
                </Badge>
              </div>
              <div className="flex gap-2">
                <Button size="sm" onClick={() => verifyExpert.mutate({ id: e.id, status: "APPROVED" })}>
                  Approve Verification
                </Button>
                <Button size="sm" variant="outline" onClick={() => verifyExpert.mutate({ id: e.id, status: "REJECTED" })}>
                  Reject
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      {tab === "suppliers" && (
        <div className="grid gap-4 md:grid-cols-2">
          {(suppliers || []).map((s) => (
            <Card key={s.id} className="space-y-3">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="font-bold">{s.businessName}</h3>
                  <p className="text-xs text-[#4B6358]">{s.city}, {s.state} {s.gstin ? `• GSTIN: ${s.gstin}` : ""}</p>
                </div>
                <Badge tone={s.verificationStatus === "APPROVED" ? "green" : "amber"}>
                  {s.verificationStatus}
                </Badge>
              </div>
              <div className="flex gap-2">
                <Button size="sm" onClick={() => verifySupplier.mutate({ id: s.id, status: "APPROVED" })}>
                  Approve Supplier
                </Button>
                <Button size="sm" variant="outline" onClick={() => verifySupplier.mutate({ id: s.id, status: "REJECTED" })}>
                  Reject
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
