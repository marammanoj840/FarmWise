import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { KeyRound, ShieldCheck, Trash2, UserCircle } from "lucide-react";
import { authApi, userApi } from "@/api/services";
import { useAuth } from "@/context/AuthContext";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage, formatDateTime } from "@/utils/format";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { Breadcrumbs } from "@/components/ui/Feedback";

export function ProfileSettingsPage() {
  const queryClient = useQueryClient();
  const { refreshUser } = useAuth();
  const { success, error } = useToast();
  const [devVerifyToken, setDevVerifyToken] = useState<string | null>(null);

  const { data: profile } = useQuery({ queryKey: ["user", "profile"], queryFn: userApi.getProfile });
  const { data: sessions } = useQuery({ queryKey: ["user", "sessions"], queryFn: userApi.getSessions });

  const profileForm = useForm({
    defaultValues: {
      fullName: "",
      phone: "",
      bio: "",
      addressLine: "",
      villageOrCity: "",
      district: "",
      state: "",
      country: "India",
      postalCode: "",
      preferredLanguage: "en",
      preferredCurrency: "INR",
      farmExperienceYears: 0,
    },
  });

  useEffect(() => {
    if (profile) {
      profileForm.reset({
        fullName: profile.fullName || "",
        phone: profile.phone || "",
        bio: profile.bio || "",
        addressLine: profile.addressLine || "",
        villageOrCity: profile.villageOrCity || "",
        district: profile.district || "",
        state: profile.state || "",
        country: profile.country || "India",
        postalCode: profile.postalCode || "",
        preferredLanguage: profile.preferredLanguage || "en",
        preferredCurrency: profile.preferredCurrency || "INR",
        farmExperienceYears: profile.farmExperienceYears ?? 0,
      });
    }
  }, [profile, profileForm]);

  const pwdForm = useForm({ defaultValues: { currentPassword: "", newPassword: "" } });

  const saveProfile = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      userApi.updateProfile({ ...v, farmExperienceYears: Number(v.farmExperienceYears || 0) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user", "profile"] });
      refreshUser();
      success("Profile updated");
    },
    onError: (err) => error("Failed to update profile", extractErrorMessage(err)),
  });

  const changePwd = useMutation({
    mutationFn: userApi.changePassword,
    onSuccess: () => {
      pwdForm.reset();
      success("Password updated");
    },
    onError: (err) => error("Password update failed", extractErrorMessage(err)),
  });

  const revokeSession = useMutation({
    mutationFn: userApi.revokeSession,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user", "sessions"] });
      success("Session revoked");
    },
  });

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Profile & Account Settings" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Profile & Account Settings</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Manage contact details, regional preferences, password, and active JWT refresh sessions.
          </p>
        </div>
        {profile && (
          <div className="flex items-center gap-2">
            <Badge tone="green">{profile.primaryRole}</Badge>
            <Badge tone={profile.emailVerified ? "green" : "amber"}>
              {profile.emailVerified ? "Email Verified" : "Email Unverified"}
            </Badge>
          </div>
        )}
      </div>

      {profile && !profile.emailVerified && (
        <Card className="flex flex-wrap items-center justify-between gap-3 border-amber-300 bg-amber-50/70 dark:border-amber-900 dark:bg-amber-950/30">
          <div className="text-xs">
            <p className="font-bold">Verify your email address</p>
            <p className="text-[#4B6358] dark:text-[#9EB7AB]">
              Request a fresh verification token to confirm {profile.email}.
            </p>
            {devVerifyToken && <code className="mt-1 block font-mono">Dev token: {devVerifyToken}</code>}
          </div>
          <Button
            size="sm"
            onClick={async () => {
              const res = await authApi.resendVerification();
              if (res.devResetTokenPreview) setDevVerifyToken(res.devResetTokenPreview);
              success("Verification issued", res.message);
            }}
          >
            Send Verification Token
          </Button>
        </Card>
      )}

      <div className="grid gap-6 lg:grid-cols-12">
        <Card className="space-y-4 lg:col-span-7">
          <div className="flex items-center gap-2">
            <UserCircle className="h-5 w-5 text-[#2A8C55]" />
            <h2 className="text-lg font-bold">Personal & Regional Information</h2>
          </div>
          <form onSubmit={profileForm.handleSubmit((v) => saveProfile.mutate(v))} className="space-y-4">
            <div className="grid gap-4 sm:grid-cols-2">
              <Input label="Full Name" required {...profileForm.register("fullName", { required: true })} />
              <Input label="Phone" {...profileForm.register("phone")} />
            </div>
            <div className="grid gap-4 sm:grid-cols-3">
              <Input label="Village / City" {...profileForm.register("villageOrCity")} />
              <Input label="District" {...profileForm.register("district")} />
              <Input label="State" {...profileForm.register("state")} />
            </div>
            <div className="grid gap-4 sm:grid-cols-3">
              <Input label="Country" {...profileForm.register("country")} />
              <Select label="Preferred Language" {...profileForm.register("preferredLanguage")}>
                <option value="en">English (en)</option>
                <option value="hi">Hindi (Planned)</option>
                <option value="pa">Punjabi (Planned)</option>
                <option value="mr">Marathi (Planned)</option>
                <option value="ta">Tamil (Planned)</option>
              </Select>
              <Select label="Currency" {...profileForm.register("preferredCurrency")}>
                <option value="INR">INR (₹)</option>
              </Select>
            </div>
            <Textarea label="Bio / Farm Background" {...profileForm.register("bio")} />
            <Button type="submit" loading={saveProfile.isPending}>
              Save Profile Changes
            </Button>
          </form>
        </Card>

        <div className="space-y-6 lg:col-span-5">
          <Card className="space-y-4">
            <div className="flex items-center gap-2">
              <KeyRound className="h-5 w-5 text-[#2A8C55]" />
              <h2 className="text-lg font-bold">Change Password</h2>
            </div>
            <form onSubmit={pwdForm.handleSubmit((v) => changePwd.mutate(v))} className="space-y-3">
              <Input
                label="Current Password"
                type="password"
                required
                {...pwdForm.register("currentPassword", { required: true })}
              />
              <Input
                label="New Password"
                type="password"
                minLength={8}
                required
                {...pwdForm.register("newPassword", { required: true })}
              />
              <Button type="submit" variant="secondary" loading={changePwd.isPending}>
                Update Password
              </Button>
            </form>
          </Card>

          <Card className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="inline-flex items-center gap-2 font-bold">
                <ShieldCheck className="h-5 w-5 text-[#2A8C55]" />
                Active Sessions ({sessions?.length ?? 0})
              </span>
            </div>
            <div className="space-y-2">
              {(sessions || []).map((s) => (
                <div key={s.id} className="flex items-center justify-between rounded-xl border border-[#DCE5DC] p-2.5 text-xs dark:border-[#203D31]">
                  <div className="min-w-0 flex-1 pr-2">
                    <p className="truncate font-semibold">{s.userAgent || "Browser / API Client"}</p>
                    <p className="text-[#4B6358] dark:text-[#9EB7AB]">
                      IP: {s.ipAddress || "local"} • Started {formatDateTime(s.createdAt)}
                    </p>
                  </div>
                  <Button
                    size="sm"
                    variant="ghost"
                    className="text-red-600"
                    onClick={() => revokeSession.mutate(s.id)}
                    icon={<Trash2 className="h-3.5 w-3.5" />}
                  />
                </div>
              ))}
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
