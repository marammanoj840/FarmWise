import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  AlertCircle,
  CheckCircle2,
  Info,
  KeyRound,
  Loader2,
  MailCheck,
  MapPin,
  RefreshCw,
  UserPlus,
} from "lucide-react";
import { authApi } from "@/api/services";
import { getHomePathForRole, useAuth } from "@/context/AuthContext";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage } from "@/utils/format";
import {
  detectCurrentLocation,
  type GeolocationState,
  type GeolocationStatus,
} from "@/utils/geolocation";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Select } from "@/components/ui/Field";

const loginSchema = z.object({
  email: z.string().email("Enter a valid email"),
  password: z.string().min(1, "Password is required"),
});

const registerSchema = z.object({
  fullName: z.string().min(2, "Full name must be at least 2 characters"),
  email: z.string().email("Enter a valid email"),
  password: z.string().min(8, "Password must be at least 8 characters"),
  phone: z.string().optional(),
  role: z.enum(["FARMER", "EXPERT", "SUPPLIER", "BUYER"]),
  organizationOrBusinessName: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  specializations: z.string().optional(),
});

export function LoginPage() {
  const { login } = useAuth();
  const { success, error } = useToast();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const redirectTo = searchParams.get("redirect");

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<z.infer<typeof loginSchema>>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (values: z.infer<typeof loginSchema>) => {
    try {
      const auth = await login(values.email, values.password);
      success("Signed in", `Welcome back, ${auth.user.fullName}`);
      navigate(redirectTo || getHomePathForRole(auth.user.primaryRole), { replace: true });
    } catch (err) {
      error("Sign in failed", extractErrorMessage(err, "Invalid email or password."));
    }
  };

  const fillDemo = (email: string) => {
    setValue("email", email);
    setValue("password", "FarmWiseDemo#2026");
  };

  return (
    <div className="mx-auto max-w-md px-4 py-14 sm:px-6">
      <Card className="space-y-6">
        <div className="space-y-1.5 text-center">
          <Badge tone="green">Secure Portal Access</Badge>
          <h1 className="font-display text-3xl font-bold">Sign in to FarmWise</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Access your farms, marketplace, advisory, or administration dashboard.
          </p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input label="Email" type="email" required {...register("email")} error={errors.email?.message} />
          <Input label="Password" type="password" required {...register("password")} error={errors.password?.message} />
          <div className="flex items-center justify-between text-xs">
            <Link to="/verify-email" className="font-medium text-[#2A8C55] hover:underline">
              Verify email token
            </Link>
            <Link to="/forgot-password" className="font-medium text-[#2A8C55] hover:underline">
              Forgot password?
            </Link>
          </div>
          <Button type="submit" loading={isSubmitting} className="w-full" size="lg">
            Sign In
          </Button>
        </form>

        {import.meta.env.DEV && (
          <div className="rounded-xl border border-amber-200 bg-amber-50/70 p-3.5 text-xs dark:border-amber-900/60 dark:bg-amber-950/30">
            <div className="mb-2 flex items-center gap-1.5 font-semibold text-amber-900 dark:text-amber-300">
              <Info className="h-4 w-4" />
              Development Demo Accounts (Quick Fill)
            </div>
            <div className="flex flex-wrap gap-1.5">
              {[
                { label: "Farmer", email: "farmer.demo@farmwise.in" },
                { label: "Expert", email: "expert.demo@farmwise.in" },
                { label: "Supplier", email: "supplier.demo@farmwise.in" },
                { label: "Buyer", email: "buyer.demo@farmwise.in" },
                { label: "Admin", email: "admin.demo@farmwise.in" },
              ].map((d) => (
                <button
                  key={d.label}
                  type="button"
                  onClick={() => fillDemo(d.email)}
                  className="rounded-lg border border-amber-300 bg-white px-2.5 py-1 font-medium text-amber-950 hover:bg-amber-100 dark:border-amber-800 dark:bg-[#0F211A] dark:text-amber-200"
                >
                  {d.label}
                </button>
              ))}
            </div>
          </div>
        )}

        <p className="text-center text-sm text-[#4B6358] dark:text-[#9EB7AB]">
          New to FarmWise?{" "}
          <Link to="/register" className="font-semibold text-[#10402E] hover:underline dark:text-[#4FD085]">
            Create an account
          </Link>
        </p>
      </Card>
    </div>
  );
}

export function RegisterPage() {
  const { register: registerAccount } = useAuth();
  const { success, error } = useToast();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const defaultRoleParam = (searchParams.get("role") || "FARMER").toUpperCase();
  const initialRole = (["FARMER", "EXPERT", "SUPPLIER", "BUYER"].includes(defaultRoleParam)
    ? defaultRoleParam
    : "FARMER") as "FARMER" | "EXPERT" | "SUPPLIER" | "BUYER";

  const {
    register,
    watch,
    handleSubmit,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<z.infer<typeof registerSchema>>({
    resolver: zodResolver(registerSchema),
    defaultValues: { role: initialRole },
  });

  const selectedRole = watch("role");
  const [verifyPreview, setVerifyPreview] = useState<string | null>(null);
  const [targetHome, setTargetHome] = useState<string>("/app/farmer");
  const [geoState, setGeoState] = useState<GeolocationState>({ status: "idle" });

  const handleDetectLocation = async () => {
    try {
      setGeoState({ status: "locating", message: "Requesting device location..." });
      const detected = await detectCurrentLocation({
        timeoutMs: 12000,
        onStatusChange: (status, message) => {
          setGeoState({ status, message });
        },
      });

      const cityToFill = detected.city || detected.district || "";
      if (cityToFill) {
        setValue("city", cityToFill, { shouldValidate: true, shouldDirty: true });
      }
      if (detected.state) {
        setValue("state", detected.state, { shouldValidate: true, shouldDirty: true });
      }

      const locationSummary = [detected.city, detected.state, detected.country]
        .filter(Boolean)
        .join(", ");

      setGeoState({
        status: "success",
        message: locationSummary,
        result: detected,
      });
      success("Location detected", `Auto-filled: ${locationSummary}`);
    } catch (err: unknown) {
      const customErr = err as { code?: GeolocationStatus; message?: string };
      const status = customErr.code || "error";
      const message = customErr.message || "Unable to detect location. Please enter manually.";
      setGeoState({ status, message });
    }
  };

  const onSubmit = async (values: z.infer<typeof registerSchema>) => {
    try {
      const auth = await registerAccount(values);
      success("Account created", `Welcome to FarmWise, ${auth.user.fullName}!`);
      const home = getHomePathForRole(auth.user.primaryRole);
      if (auth.devEmailVerificationToken) {
        setVerifyPreview(auth.devEmailVerificationToken);
        setTargetHome(home);
      } else {
        navigate(home, { replace: true });
      }
    } catch (err) {
      error("Registration failed", extractErrorMessage(err));
    }
  };

  if (verifyPreview) {
    return (
      <div className="mx-auto max-w-md px-4 py-14">
        <Card className="space-y-4 text-center">
          <MailCheck className="mx-auto h-10 w-10 text-[#2A8C55]" />
          <h1 className="font-display text-2xl font-bold">Account Created</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            In development mode (no live SMTP configured), your email verification token is shown below:
          </p>
          <code className="block break-all rounded-xl bg-[#EEF3EE] p-3 text-xs font-mono dark:bg-[#152B22]">
            {verifyPreview}
          </code>
          <div className="flex flex-col gap-2">
            <Button
              onClick={async () => {
                await authApi.verifyEmail(verifyPreview);
                success("Email verified");
                navigate(targetHome, { replace: true });
              }}
            >
              Verify Email & Continue to Portal
            </Button>
            <Button variant="outline" onClick={() => navigate(targetHome, { replace: true })}>
              Skip for Now
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-lg px-4 py-14 sm:px-6">
      <Card className="space-y-6">
        <div className="space-y-1.5 text-center">
          <Badge tone="green">Join the Ecosystem</Badge>
          <h1 className="font-display text-3xl font-bold">Create your FarmWise account</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Choose your agricultural role to unlock your tailored workspace.
          </p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Select label="Account Role" required {...register("role")}>
            <option value="FARMER">Farmer / Producer</option>
            <option value="EXPERT">Agricultural Expert / Agronomist</option>
            <option value="SUPPLIER">Agri Input Supplier</option>
            <option value="BUYER">Produce Buyer / Procurement</option>
          </Select>

          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Full Name" required {...register("fullName")} error={errors.fullName?.message} />
            <Input label="Phone (Optional)" placeholder="+91-..." {...register("phone")} />
          </div>

          <Input label="Email Address" type="email" required {...register("email")} error={errors.email?.message} />
          <Input
            label="Password"
            type="password"
            required
            hint="Minimum 8 characters"
            {...register("password")}
            error={errors.password?.message}
          />

          {(selectedRole === "SUPPLIER" || selectedRole === "BUYER" || selectedRole === "EXPERT") && (
            <Input
              label={
                selectedRole === "EXPERT"
                  ? "Professional Headline"
                  : selectedRole === "SUPPLIER"
                    ? "Store / Business Name"
                    : "Organization Name"
              }
              {...register("organizationOrBusinessName")}
            />
          )}

          {selectedRole === "EXPERT" && (
            <Input
              label="Areas of Expertise"
              placeholder="e.g., Soil Fertility, Cotton IPM, Drip Irrigation"
              {...register("specializations")}
            />
          )}

          <div className="space-y-3">
            <div className="grid gap-4 sm:grid-cols-2">
              <Input
                label="City / Village"
                placeholder="e.g., Guntur, Nashik, Karnal"
                {...register("city")}
              />
              <Input
                label="State"
                placeholder="e.g., Andhra Pradesh, Maharashtra"
                {...register("state")}
              />
            </div>

            <div className="flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <button
                  type="button"
                  onClick={handleDetectLocation}
                  disabled={geoState.status === "locating" || geoState.status === "geocoding"}
                  className="inline-flex items-center gap-1.5 rounded-lg border border-[#CDE0D5] bg-[#F2F7F4] px-3 py-1.5 text-xs font-semibold text-[#185339] transition-colors hover:bg-[#E3EEE7] focus:outline-none focus:ring-2 focus:ring-[#2A8C55] focus:ring-offset-1 disabled:cursor-not-allowed disabled:opacity-60 dark:border-[#224434] dark:bg-[#132B20] dark:text-[#52D888] dark:hover:bg-[#1B3A2C]"
                  aria-label="Use my current location to auto-fill city and state"
                >
                  {geoState.status === "locating" || geoState.status === "geocoding" ? (
                    <Loader2 className="h-3.5 w-3.5 animate-spin text-[#2A8C55] dark:text-[#52D888]" />
                  ) : (
                    <MapPin className="h-3.5 w-3.5 text-[#2A8C55] dark:text-[#52D888]" />
                  )}
                  <span>
                    {geoState.status === "locating" || geoState.status === "geocoding"
                      ? "Detecting location..."
                      : "📍 Use My Current Location"}
                  </span>
                </button>

                {(geoState.status === "timeout" ||
                  geoState.status === "error" ||
                  geoState.status === "position_unavailable") && (
                  <button
                    type="button"
                    onClick={handleDetectLocation}
                    className="inline-flex items-center gap-1 text-xs font-medium text-[#2A8C55] hover:underline dark:text-[#52D888]"
                  >
                    <RefreshCw className="h-3 w-3" />
                    <span>Retry Location</span>
                  </button>
                )}
              </div>

              {/* Success Notification Banner */}
              {geoState.status === "success" && (
                <div
                  role="status"
                  aria-live="polite"
                  className="flex items-start gap-2 rounded-lg border border-emerald-200 bg-emerald-50/80 p-2.5 text-xs text-emerald-900 dark:border-emerald-900/60 dark:bg-emerald-950/30 dark:text-emerald-200"
                >
                  <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-emerald-600 dark:text-emerald-400" />
                  <div className="space-y-0.5">
                    <p className="font-semibold">Location detected & auto-filled</p>
                    <p className="text-emerald-800 dark:text-emerald-300">
                      {geoState.message}
                    </p>
                  </div>
                </div>
              )}

              {/* Permission Denied Notification Banner */}
              {geoState.status === "permission_denied" && (
                <div
                  role="status"
                  aria-live="polite"
                  className="flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50/80 p-2.5 text-xs text-amber-900 dark:border-amber-900/60 dark:bg-amber-950/30 dark:text-amber-200"
                >
                  <Info className="mt-0.5 h-4 w-4 shrink-0 text-amber-600 dark:text-amber-400" />
                  <div className="space-y-0.5">
                    <p className="font-semibold">Location access not granted</p>
                    <p className="text-amber-800 dark:text-amber-300">
                      {geoState.message}
                    </p>
                  </div>
                </div>
              )}

              {/* Timeout, Position Unavailable or Error Notification Banner */}
              {(geoState.status === "timeout" ||
                geoState.status === "position_unavailable" ||
                geoState.status === "error" ||
                geoState.status === "unsupported") && (
                <div
                  role="alert"
                  aria-live="polite"
                  className="flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50/80 p-2.5 text-xs text-amber-900 dark:border-amber-900/60 dark:bg-amber-950/30 dark:text-amber-200"
                >
                  <AlertCircle className="mt-0.5 h-4 w-4 shrink-0 text-amber-600 dark:text-amber-400" />
                  <div className="space-y-0.5">
                    <p className="font-semibold">Manual entry available</p>
                    <p className="text-amber-800 dark:text-amber-300">
                      {geoState.message}
                    </p>
                  </div>
                </div>
              )}
            </div>
          </div>

          <Button type="submit" loading={isSubmitting} className="w-full" size="lg" icon={<UserPlus className="h-4 w-4" />}>
            Create Account
          </Button>
        </form>

        <p className="text-center text-sm text-[#4B6358] dark:text-[#9EB7AB]">
          Already registered?{" "}
          <Link to="/login" className="font-semibold text-[#10402E] hover:underline dark:text-[#4FD085]">
            Sign in
          </Link>
        </p>
      </Card>
    </div>
  );
}

export function ForgotPasswordPage() {
  const { success, error } = useToast();
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [previewToken, setPreviewToken] = useState<string | null>(null);
  const [done, setDone] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await authApi.forgotPassword(email);
      setDone(true);
      setPreviewToken(res.devResetTokenPreview ?? null);
      success("Instructions issued", res.message);
    } catch (err) {
      error("Request failed", extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-md px-4 py-14">
      <Card className="space-y-5">
        <div className="text-center">
          <KeyRound className="mx-auto mb-2 h-8 w-8 text-[#2A8C55]" />
          <h1 className="font-display text-2xl font-bold">Reset Password</h1>
          <p className="mt-1 text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Enter your account email to generate a password reset token.
          </p>
        </div>

        {done ? (
          <div className="space-y-4">
            <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
              If an account exists for <strong>{email}</strong>, reset instructions have been issued.
            </p>
            {previewToken && (
              <div className="rounded-xl bg-[#EEF3EE] p-3 text-xs dark:bg-[#152B22]">
                <p className="mb-1 font-semibold">Dev Reset Token Preview:</p>
                <code className="break-all font-mono">{previewToken}</code>
              </div>
            )}
            <Link to={previewToken ? `/reset-password?token=${encodeURIComponent(previewToken)}` : "/reset-password"}>
              <Button className="w-full">Proceed to Reset Password</Button>
            </Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Account Email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
            <Button type="submit" loading={loading} className="w-full">
              Send Reset Instructions
            </Button>
          </form>
        )}
      </Card>
    </div>
  );
}

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { success, error } = useToast();
  const [token, setToken] = useState(searchParams.get("token") || "");
  const [newPassword, setNewPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await authApi.resetPassword({ token, newPassword });
      success("Password reset", "You can now sign in with your new password.");
      navigate("/login");
    } catch (err) {
      error("Reset failed", extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-md px-4 py-14">
      <Card className="space-y-5">
        <h1 className="font-display text-2xl font-bold">Set New Password</h1>
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input label="Reset Token" required value={token} onChange={(e) => setToken(e.target.value)} />
          <Input
            label="New Password"
            type="password"
            minLength={8}
            required
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
          />
          <Button type="submit" loading={loading} className="w-full">
            Update Password
          </Button>
        </form>
      </Card>
    </div>
  );
}

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const { success, error } = useToast();
  const [token, setToken] = useState(searchParams.get("token") || "");
  const [verified, setVerified] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await authApi.verifyEmail(token);
      setVerified(true);
      success("Email verified", "Your account email is now verified.");
    } catch (err) {
      error("Verification failed", extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-md px-4 py-14">
      <Card className="space-y-5">
        <h1 className="font-display text-2xl font-bold">Verify Email</h1>
        {verified ? (
          <div className="space-y-4 text-center">
            <CheckCircle2 className="mx-auto h-10 w-10 text-[#2A8C55]" />
            <p className="text-sm">Your email address is verified.</p>
            <Link to="/login">
              <Button className="w-full">Continue to Sign In</Button>
            </Link>
          </div>
        ) : (
          <form onSubmit={handleVerify} className="space-y-4">
            <Input label="Verification Token" required value={token} onChange={(e) => setToken(e.target.value)} />
            <Button type="submit" loading={loading} className="w-full">
              Verify Email
            </Button>
          </form>
        )}
      </Card>
    </div>
  );
}
