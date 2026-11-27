import { useEffect } from "react";
import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { motion } from "motion/react";
import Lenis from "lenis";
import {
  ArrowRight,
  Bot,
  CheckCircle2,
  CloudSun,
  FlaskConical,
  IndianRupee,
  Layers,
  ShieldCheck,
  ShoppingBag,
  Sprout,
  TrendingUp,
  Users,
} from "lucide-react";
import { publicApi } from "@/api/services";
import { FarmHero3D } from "@/components/shared/FarmHero3D";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";

const PLATFORM_PILLARS = [
  {
    icon: <Sprout className="h-5 w-5" />,
    title: "Farm, Crop & Cultivation Logs",
    desc: "Manage multiple land holdings, varieties, sowing dates, and all 9 cultivation stages with task reminders.",
  },
  {
    icon: <IndianRupee className="h-5 w-5" />,
    title: "INR Finance & P&L Reports",
    desc: "Track crop-wise expenses, harvest revenues, inventory valuations, and export PDF/CSV reports cleanly.",
  },
  {
    icon: <CloudSun className="h-5 w-5" />,
    title: "Open-Meteo Weather Intelligence",
    desc: "Farm-coordinate forecasts, spraying wind checks, heat & rain alerts without fabricated telemetry.",
  },
  {
    icon: <FlaskConical className="h-5 w-5" />,
    title: "Soil Health & Crop Lab",
    desc: "Record NPK and pH tests, log field symptoms, and explore our extensible Indian crop knowledge base.",
  },
  {
    icon: <Bot className="h-5 w-5" />,
    title: "AI-Ready Farm Assistant",
    desc: "Context-aware Q&A linked to your farms and crops, with transparent agronomic reference fallback.",
  },
  {
    icon: <ShoppingBag className="h-5 w-5" />,
    title: "Input & Produce Marketplace",
    desc: "Connect directly with verified input suppliers and bulk produce buyers with transparent inquiry workflows.",
  },
];

const DEMO_TESTIMONIALS = [
  {
    name: "Harpreet Singh Gill",
    role: "Demo Profile • Wheat & Paddy Farmer, Ludhiana",
    quote:
      "Having our soil pH logs, CRI irrigation schedule, and crop-wise input costs in one ledger removed guesswork from Rabi planning.",
  },
  {
    name: "Dr. Kavita Kulkarni",
    role: "Demo Profile • Soil Fertility Agronomist, Pune",
    quote:
      "Farmers share exact soil test numbers and field observations in consultations, making remote advisory structured and accountable.",
  },
  {
    name: "Sahyadri Fresh Procurement",
    role: "Demo Profile • Regional Produce Buyer, Mumbai",
    quote:
      "Direct harvest-grade produce listings with transparent quantity and mandi benchmarks streamline farm-gate sourcing.",
  },
];

export function HomePage() {
  const { data: overview } = useQuery({
    queryKey: ["public", "overview"],
    queryFn: publicApi.overview,
    retry: 1,
  });

  useEffect(() => {
    const prefersReduced = window.matchMedia?.("(prefers-reduced-motion: reduce)").matches;
    if (prefersReduced) return;
    const lenis = new Lenis({ duration: 1.1, smoothWheel: true });
    let rafId = 0;
    const raf = (time: number) => {
      lenis.raf(time);
      rafId = requestAnimationFrame(raf);
    };
    rafId = requestAnimationFrame(raf);
    return () => {
      cancelAnimationFrame(rafId);
      lenis.destroy();
    };
  }, []);

  return (
    <div>
      {/* Hero Section */}
      <section className="fw-hero-mesh relative overflow-hidden border-b border-[#DCE5DC] dark:border-[#203D31]">
        <div className="mx-auto grid max-w-7xl items-center gap-10 px-4 py-14 sm:px-6 md:py-20 lg:grid-cols-12 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.55 }}
            className="space-y-6 lg:col-span-6"
          >
            <Badge tone="green">Built for Indian Agriculture • INR (₹) Ecosystem</Badge>
            <h1 className="font-display text-4xl font-bold leading-tight tracking-tight text-[#0D2E20] dark:text-[#F0F7F2] sm:text-5xl lg:text-[3.35rem]">
              Smarter Farming. Better Harvests. Stronger Futures.
            </h1>
            <p className="max-w-xl text-base leading-relaxed text-[#425B50] dark:text-[#AFC6BA] sm:text-lg">
              FarmWise unites farm management, crop lifecycle tracking, soil records, Open-Meteo weather advisories,
              INR finance ledgers, expert consultations, and direct agricultural commerce in one cohesive platform.
            </p>
            <div className="flex flex-wrap items-center gap-3.5">
              <Link to="/register">
                <Button size="lg" icon={<ArrowRight className="h-4 w-4" />}>
                  Create Free Account
                </Button>
              </Link>
              <Link to="/marketplace">
                <Button size="lg" variant="outline">
                  Explore Marketplace
                </Button>
              </Link>
            </div>
            <div className="grid grid-cols-3 gap-4 border-t border-[#DCE5DC] pt-6 dark:border-[#203D31]">
              <div>
                <p className="font-display text-2xl font-bold text-[#10402E] dark:text-[#56CF88]">
                  {overview?.supportedCrops ?? 10}+
                </p>
                <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">Cataloged Indian Crops</p>
              </div>
              <div>
                <p className="font-display text-2xl font-bold text-[#10402E] dark:text-[#56CF88]">5 Roles</p>
                <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">Farmer, Expert, Supplier, Buyer, Admin</p>
              </div>
              <div>
                <p className="font-display text-2xl font-bold text-[#10402E] dark:text-[#56CF88]">100%</p>
                <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">Transparent Data & Exports</p>
              </div>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, scale: 0.97 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.6, delay: 0.1 }}
            className="lg:col-span-6"
          >
            <FarmHero3D />
          </motion.div>
        </div>
      </section>

      {/* Platform Features Overview */}
      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <div className="mb-10 flex flex-wrap items-end justify-between gap-4">
          <div className="max-w-2xl">
            <Badge tone="amber">Complete Ecosystem</Badge>
            <h2 className="mt-3 font-display text-3xl font-bold tracking-tight sm:text-4xl">
              Every operational tool a modern farm needs
            </h2>
          </div>
          <Link to="/features">
            <Button variant="outline">View All Features</Button>
          </Link>
        </div>
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {PLATFORM_PILLARS.map((item) => (
            <Card key={item.title} className="transition hover:-translate-y-0.5 hover:border-[#2A8C55]/50">
              <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-[#EEF3EE] text-[#10402E] dark:bg-[#173026] dark:text-[#4FD085]">
                {item.icon}
              </div>
              <h3 className="text-lg font-bold">{item.title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-[#4B6358] dark:text-[#9EB7AB]">{item.desc}</p>
            </Card>
          ))}
        </div>
      </section>

      {/* How FarmWise Works */}
      <section className="border-y border-[#DCE5DC] bg-white/70 py-16 dark:border-[#203D31] dark:bg-[#0D1E17]/70">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="mx-auto max-w-2xl text-center">
            <Badge tone="green">Workflow</Badge>
            <h2 className="mt-3 font-display text-3xl font-bold sm:text-4xl">How FarmWise Works</h2>
            <p className="mt-2 text-sm text-[#4B6358] dark:text-[#9EB7AB]">
              From field setup to harvest sale — organized cleanly in four connected steps.
            </p>
          </div>
          <div className="mt-10 grid gap-6 md:grid-cols-4">
            {[
              { step: "01", title: "Register Farms & Plots", desc: "Add land area, soil profile, irrigation setup, and village coordinates." },
              { step: "02", title: "Track Crops & Operations", desc: "Log varieties, sowing dates, irrigation, fertilization, and field tasks." },
              { step: "03", title: "Monitor Soil, Weather & Costs", desc: "Review Open-Meteo forecasts, lab NPK/pH trends, and INR profit/loss." },
              { step: "04", title: "Consult & Trade Directly", desc: "Connect with verified agronomists, trusted input suppliers, and bulk buyers." },
            ].map((s) => (
              <Card key={s.step}>
                <span className="font-display text-3xl font-bold text-[#2A8C55] dark:text-[#4FD085]">{s.step}</span>
                <h3 className="mt-3 text-base font-bold">{s.title}</h3>
                <p className="mt-1.5 text-sm text-[#4B6358] dark:text-[#9EB7AB]">{s.desc}</p>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Technology & Trust */}
      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <div className="grid items-center gap-10 lg:grid-cols-2">
          <div className="space-y-5">
            <Badge tone="blue">Architecture & Integrity</Badge>
            <h2 className="font-display text-3xl font-bold sm:text-4xl">
              Honest data, role-isolated security, and integration readiness
            </h2>
            <p className="text-sm leading-relaxed text-[#4B6358] dark:text-[#9EB7AB]">
              FarmWise never fabricates live weather or mandi feeds, never claims unverified experts are certified, and
              separates financial estimates from guaranteed outcomes.
            </p>
            <ul className="space-y-3 text-sm">
              {[
                "Strict farm ownership isolation backed by Spring Security & JWT",
                "Open-Meteo weather integration with transparent fallback states",
                "Agmarknet/eNAM & AI provider interfaces ready for live credentials",
                "Downloadable PDF & CSV audit-ready operational reports",
              ].map((line) => (
                <li key={line} className="flex items-start gap-2.5">
                  <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-[#2A8C55] dark:text-[#4FD085]" />
                  <span>{line}</span>
                </li>
              ))}
            </ul>
            <Link to="/solutions">
              <Button variant="secondary" className="mt-2">
                Explore Stakeholder Portals
              </Button>
            </Link>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Card>
              <Layers className="mb-2 h-6 w-6 text-[#10402E] dark:text-[#4FD085]" />
              <h4 className="font-bold">PostgreSQL + Flyway</h4>
              <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                Normalized schema with 35+ agricultural, commerce, and governance tables.
              </p>
            </Card>
            <Card>
              <ShieldCheck className="mb-2 h-6 w-6 text-[#10402E] dark:text-[#4FD085]" />
              <h4 className="font-bold">RBAC & Audit Logs</h4>
              <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                Server-side role verification across Farmer, Expert, Supplier, Buyer, and Admin.
              </p>
            </Card>
            <Card>
              <TrendingUp className="mb-2 h-6 w-6 text-[#10402E] dark:text-[#4FD085]" />
              <h4 className="font-bold">Mandi Benchmarks</h4>
              <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                Commodity & state comparison charts clearly marked when sample reference data is shown.
              </p>
            </Card>
            <Card>
              <Users className="mb-2 h-6 w-6 text-[#10402E] dark:text-[#4FD085]" />
              <h4 className="font-bold">Expert Verification</h4>
              <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                Admin-verified advisory badges only displayed after explicit credential approval.
              </p>
            </Card>
          </div>
        </div>
      </section>

      {/* Demo Testimonials */}
      <section className="border-t border-[#DCE5DC] bg-[#EEF3EE]/70 py-16 dark:border-[#203D31] dark:bg-[#0D1E17]">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
            <div>
              <Badge tone="amber">Clearly Identified Demo Stories</Badge>
              <h2 className="mt-2 font-display text-3xl font-bold">Ecosystem Perspectives (Demo Content)</h2>
            </div>
            <Link to="/faq">
              <Button variant="outline">Read FAQ</Button>
            </Link>
          </div>
          <div className="grid gap-6 md:grid-cols-3">
            {DEMO_TESTIMONIALS.map((t) => (
              <Card key={t.name}>
                <p className="text-sm italic leading-relaxed text-[#2C473C] dark:text-[#CBE0D4]">“{t.quote}”</p>
                <div className="mt-5 border-t border-[#DCE5DC] pt-3 dark:border-[#203D31]">
                  <p className="text-sm font-bold">{t.name}</p>
                  <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">{t.role}</p>
                </div>
              </Card>
            ))}
          </div>

          <Card className="mt-12 bg-gradient-to-r from-[#10402E] to-[#1D6B4B] text-white dark:from-[#154A36] dark:to-[#227653]" padding="p-8 sm:p-10">
            <div className="flex flex-col items-start justify-between gap-6 md:flex-row md:items-center">
              <div className="max-w-xl">
                <h3 className="font-display text-2xl font-bold sm:text-3xl">Ready to digitize your agricultural operations?</h3>
                <p className="mt-2 text-sm text-emerald-100">
                  Register as a Farmer, Agricultural Expert, Input Supplier, or Bulk Buyer — or reach out for FPO & agribusiness onboarding.
                </p>
              </div>
              <div className="flex flex-wrap gap-3">
                <Link to="/register">
                  <Button className="bg-white text-[#10402E] hover:bg-emerald-50 dark:bg-white dark:text-[#10402E]">
                    Get Started Free
                  </Button>
                </Link>
                <Link to="/contact">
                  <Button variant="outline" className="border-white/40 text-white hover:bg-white/10">
                    Contact Team
                  </Button>
                </Link>
              </div>
            </div>
          </Card>
        </div>
      </section>
    </div>
  );
}
