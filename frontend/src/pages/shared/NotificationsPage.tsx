import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Bell, Check, CheckCheck, Info } from "lucide-react";
import { notificationApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage, formatDateTime } from "@/utils/format";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Breadcrumbs, EmptyState } from "@/components/ui/Feedback";

export function NotificationsPage() {
  const queryClient = useQueryClient();
  const { success, error } = useToast();
  const [unreadOnly, setUnreadOnly] = useState(false);

  const { data: list } = useQuery({
    queryKey: ["notifications", unreadOnly],
    queryFn: () => notificationApi.list(unreadOnly ? true : undefined, 0, 50),
  });
  const { data: prefs } = useQuery({
    queryKey: ["notifications", "preferences"],
    queryFn: notificationApi.preferences,
  });

  const markRead = useMutation({
    mutationFn: notificationApi.markRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
    },
  });

  const markAllRead = useMutation({
    mutationFn: notificationApi.markAllRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
      success("All notifications marked as read");
    },
  });

  const updatePref = useMutation({
    mutationFn: (patch: Record<string, boolean>) =>
      notificationApi.updatePreferences({ ...prefs!, ...patch }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications", "preferences"] });
      success("Preferences updated");
    },
    onError: (err) => error("Failed to update preferences", extractErrorMessage(err)),
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Notifications & Preferences" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Notifications & Alerts</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Stay updated on cultivation tasks, low-stock thresholds, weather warnings, orders, and expert replies.
          </p>
        </div>
        <div className="flex gap-2">
          <Button
            size="sm"
            variant={unreadOnly ? "primary" : "outline"}
            onClick={() => setUnreadOnly((v) => !v)}
          >
            {unreadOnly ? "Showing Unread" : "Filter Unread"}
          </Button>
          <Button size="sm" variant="secondary" onClick={() => markAllRead.mutate()} icon={<CheckCheck className="h-4 w-4" />}>
            Mark All Read
          </Button>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-12">
        <div className="space-y-3 lg:col-span-8">
          {!list || list.items.length === 0 ? (
            <EmptyState
              icon={<Bell className="h-6 w-6" />}
              title="No notifications"
              description="You are all caught up."
            />
          ) : (
            list.items.map((n) => (
              <Card
                key={n.id}
                padding="p-4"
                className={`flex items-start justify-between gap-3 ${
                  !n.read ? "border-l-4 border-l-[#2A8C55]" : "opacity-80"
                }`}
              >
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-bold">{n.title}</span>
                    <Badge tone={n.read ? "slate" : "green"}>{n.notificationType}</Badge>
                  </div>
                  <p className="mt-1 text-sm text-[#355245] dark:text-[#B9CFC3]">{n.message}</p>
                  <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">{formatDateTime(n.createdAt)}</p>
                </div>
                {!n.read && (
                  <Button size="sm" variant="ghost" onClick={() => markRead.mutate(n.id)} icon={<Check className="h-4 w-4" />}>
                    Read
                  </Button>
                )}
              </Card>
            ))
          )}
        </div>

        <Card className="h-fit space-y-4 lg:col-span-4">
          <h2 className="text-lg font-bold">Alert Preferences</h2>
          {prefs && (
            <div className="space-y-3 text-sm">
              {[
                { key: "inAppEnabled", label: "In-App Notifications" },
                { key: "emailEnabled", label: "Email Notifications" },
                { key: "smsEnabled", label: "SMS Alerts" },
                { key: "cultivationReminders", label: "Cultivation & Irrigation Reminders" },
                { key: "weatherAlerts", label: "Weather Warnings" },
                { key: "inventoryAlerts", label: "Low-Stock Inventory Alerts" },
                { key: "orderUpdates", label: "Marketplace & Order Updates" },
                { key: "expertUpdates", label: "Expert Consultation Replies" },
              ].map((item) => (
                <label key={item.key} className="flex items-center justify-between gap-2">
                  <span>{item.label}</span>
                  <input
                    type="checkbox"
                    checked={Boolean((prefs as unknown as Record<string, boolean>)[item.key])}
                    onChange={(e) => updatePref.mutate({ [item.key]: e.target.checked })}
                    className="h-4 w-4 accent-[#10402E]"
                  />
                </label>
              ))}
              {(!prefs.emailProviderConfigured || !prefs.smsProviderConfigured) && (
                <div className="flex items-start gap-2 rounded-xl bg-[#EEF3EE] p-3 text-xs text-[#4B6358] dark:bg-[#152B22] dark:text-[#9EB7AB]">
                  <Info className="mt-0.5 h-4 w-4 shrink-0" />
                  <span>
                    Email SMTP: {prefs.emailProviderConfigured ? "Configured" : "Log adapter only"} • SMS Gateway:{" "}
                    {prefs.smsProviderConfigured ? "Configured" : "Log adapter only"}
                  </span>
                </div>
              )}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
