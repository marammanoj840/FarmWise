import { Link } from "react-router-dom";
import { CheckCircle2 } from "lucide-react";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";

const STEPS = [
  {
    num: "Step 01",
    title: "Create Your Role-Based Account",
    desc: "Sign up as a Farmer, Agricultural Expert, Input Supplier, or Produce Buyer. Security roles and permissions are enforced server-side from day one.",
    points: ["BCrypt password protection", "JWT access & rotating refresh tokens", "Email verification support"],
  },
  {
    num: "Step 02",
    title: "Digitize Farms, Soil & Crop Cycles",
    desc: "Add your land holdings with village/district/coordinates, upload lab NPK & pH records, and schedule sowing-to-harvest activities.",
    points: ["Multi-farm & multi-season support", "Cultivation task deadlines", "Inventory auto low-stock alerts"],
  },
  {
    num: "Step 03",
    title: "Make Weather & Cost-Aware Decisions",
    desc: "Inspect Open-Meteo forecasts before irrigating or spraying, track every ₹ rupee of expense and revenue, and export PDF/CSV reports.",
    points: ["Live Open-Meteo weather integration", "Crop-wise & farm-wise P&L", "AI & agronomic reference assistant"],
  },
  {
    num: "Step 04",
    title: "Connect With Verified Experts & Markets",
    desc: "Submit crop questions to experts, compare mandi prices, procure quality inputs, or list harvested produce for verified buyer inquiries.",
    points: ["Verified expert credential badges", "Input product marketplace & cart", "Direct farmer-to-buyer produce inquiries"],
  },
];

export function HowItWorksPage() {
  return (
    <div className="mx-auto max-w-6xl space-y-10 px-4 py-14 sm:px-6 lg:px-8">
      <div className="max-w-3xl space-y-3">
        <Badge tone="green">Step-by-Step Guide</Badge>
        <h1 className="font-display text-4xl font-bold sm:text-5xl">How FarmWise Works</h1>
        <p className="text-base text-[#4B6358] dark:text-[#9EB7AB]">
          Designed for simplicity in the field while giving agribusinesses and administrators deep governance controls.
        </p>
      </div>

      <div className="space-y-6">
        {STEPS.map((s) => (
          <Card key={s.num}>
            <div className="grid gap-6 md:grid-cols-12 md:items-center">
              <div className="md:col-span-3">
                <span className="rounded-full bg-[#EEF3EE] px-3 py-1 text-xs font-bold text-[#10402E] dark:bg-[#173026] dark:text-[#4FD085]">
                  {s.num}
                </span>
                <h2 className="mt-3 text-xl font-bold">{s.title}</h2>
              </div>
              <div className="space-y-3 md:col-span-9">
                <p className="text-sm leading-relaxed text-[#4B6358] dark:text-[#9EB7AB]">{s.desc}</p>
                <div className="flex flex-wrap gap-3">
                  {s.points.map((pt) => (
                    <span key={pt} className="inline-flex items-center gap-1.5 text-xs font-medium">
                      <CheckCircle2 className="h-4 w-4 text-[#2A8C55] dark:text-[#4FD085]" />
                      {pt}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          </Card>
        ))}
      </div>

      <div className="flex gap-3">
        <Link to="/register">
          <Button size="lg">Get Started Now</Button>
        </Link>
        <Link to="/faq">
          <Button size="lg" variant="outline">
            View FAQ
          </Button>
        </Link>
      </div>
    </div>
  );
}
