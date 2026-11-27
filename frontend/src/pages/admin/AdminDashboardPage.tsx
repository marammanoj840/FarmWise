import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { Shield, ShoppingBag, Sprout, Users } from "lucide-react";
import { adminApi } from "@/api/services";
import { Card } from "@/components/ui/Card";
import { Breadcrumbs, Skeleton } from "@/components/ui/Feedback";
import { Button } from "@/components/ui/Button";

export function AdminDashboardPage() {
  const { data: stats, isLoading } = useQuery({ queryKey: ["admin", "stats"], queryFn: adminApi.stats });

  if (isLoading) return <Skeleton className="h-64" />;

  const roleChartData = Object.entries(stats?.usersByRole || {}).map(([role, count]) => ({
    role,
    users: count,
  }));

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Administration" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Platform Administration Overview</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Server-side RBAC governance, stakeholder verification, moderation, and system telemetry.
          </p>
        </div>
        <div className="flex gap-2">
          <Link to="/app/admin/users">
            <Button size="sm" variant="outline">
              Manage Users & Verifications
            </Button>
          </Link>
          <Link to="/app/admin/moderation">
            <Button size="sm">Moderation Queue</Button>
          </Link>
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card>
          <div className="flex items-center justify-between text-xs text-[#4B6358]">
            <span>Total / Active Users</span>
            <Users className="h-4 w-4 text-[#2A8C55]" />
          </div>
          <p className="mt-2 font-display text-3xl font-bold">
            {stats?.totalUsers ?? 0} <span className="text-sm font-normal">({stats?.activeUsers ?? 0} active)</span>
          </p>
        </Card>
        <Card>
          <div className="flex items-center justify-between text-xs text-[#4B6358]">
            <span>Farms & Active Crops</span>
            <Sprout className="h-4 w-4 text-[#2A8C55]" />
          </div>
          <p className="mt-2 font-display text-3xl font-bold">
            {stats?.totalFarms ?? 0} <span className="text-sm font-normal">({stats?.totalActiveCrops ?? 0} crops)</span>
          </p>
        </Card>
        <Card>
          <div className="flex items-center justify-between text-xs text-[#4B6358]">
            <span>Products & Orders</span>
            <ShoppingBag className="h-4 w-4 text-amber-600" />
          </div>
          <p className="mt-2 font-display text-3xl font-bold">
            {stats?.totalProducts ?? 0} <span className="text-sm font-normal">({stats?.pendingProducts ?? 0} pending)</span>
          </p>
        </Card>
        <Card>
          <div className="flex items-center justify-between text-xs text-[#4B6358]">
            <span>Open Contact Inquiries</span>
            <Shield className="h-4 w-4 text-[#2A8C55]" />
          </div>
          <p className="mt-2 font-display text-3xl font-bold">{stats?.openContactInquiries ?? 0}</p>
        </Card>
      </div>

      <Card>
        <h2 className="mb-4 text-lg font-bold">Users by Platform Role</h2>
        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={roleChartData}>
              <CartesianGrid strokeDasharray="3 3" opacity={0.25} />
              <XAxis dataKey="role" fontSize={12} />
              <YAxis allowDecimals={false} fontSize={12} />
              <Tooltip />
              <Bar dataKey="users" fill="#10402E" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </Card>
    </div>
  );
}
