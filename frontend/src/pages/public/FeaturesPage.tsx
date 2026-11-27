import { Link } from "react-router-dom";
import {
  Bell,
  Bot,
  CalendarCheck,
  CloudSun,
  FileSpreadsheet,
  FlaskConical,
  IndianRupee,
  MapPin,
  Package,
  Shield,
  ShoppingBag,
  TrendingUp,
} from "lucide-react";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";

const FEATURES = [
  { icon: <MapPin />, title: "Multi-Farm Registry", desc: "Record land area, units (Acres/Hectares/Bigha), soil types, irrigation modes, coordinates, and farm photos." },
  { icon: <CalendarCheck />, title: "9-Stage Cultivation Log", desc: "Track land prep, seed selection, sowing, irrigation, fertilization, pest & disease control, monitoring, and harvest." },
  { icon: <Package />, title: "Inventory & Low-Stock Alerts", desc: "Track seeds, fertilizers, pesticides, equipment, and irrigation supplies with purchase/usage transactions." },
  { icon: <IndianRupee />, title: "INR Expenses, Income & P&L", desc: "Crop-wise and farm-wise cost breakdowns with clear disclaimer separating estimates from guaranteed returns." },
  { icon: <FileSpreadsheet />, title: "PDF & CSV Reports", desc: "One-click downloadable reports for farms, crops, inventory, expenses, harvests, and field activities." },
  { icon: <CloudSun />, title: "Open-Meteo Weather & Alerts", desc: "Farm-location temperature, humidity, rainfall, wind speed, 5-day forecasts, and field advisories." },
  { icon: <FlaskConical />, title: "Soil & Crop Intelligence", desc: "Soil pH/NPK records, fertility notes, field observations, and an extensible Indian crop knowledge base." },
  { icon: <Bot />, title: "AI Agricultural Assistant", desc: "Provider-independent Q&A with authorized farm context and transparent non-AI reference fallback." },
  { icon: <ShoppingBag />, title: "Agri Inputs & Produce Commerce", desc: "Browse seeds, tools, and drip kits from suppliers or list harvested produce directly for bulk buyers." },
  { icon: <TrendingUp />, title: "Mandi Market Price Intelligence", desc: "Search commodity and state prices, view charts, and connect Agmarknet/eNAM credentials." },
  { icon: <Bell />, title: "Smart Notifications & Reminders", desc: "Cultivation, irrigation, inventory, weather, and order updates with per-channel preferences." },
  { icon: <Shield />, title: "Admin Governance & Audit Logs", desc: "Role-based security, expert/supplier verification, product moderation, and immutable audit trails." },
];

export function FeaturesPage() {
  return (
    <div className="mx-auto max-w-7xl space-y-10 px-4 py-14 sm:px-6 lg:px-8">
      <div className="max-w-3xl space-y-3">
        <Badge tone="green">Platform Capabilities</Badge>
        <h1 className="font-display text-4xl font-bold sm:text-5xl">Complete Agricultural Technology Stack</h1>
        <p className="text-base text-[#4B6358] dark:text-[#9EB7AB]">
          Every module is backed by live PostgreSQL persistence, Spring Boot REST APIs, and responsive TypeScript views.
        </p>
      </div>

      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {FEATURES.map((f) => (
          <Card key={f.title}>
            <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-[#EEF3EE] text-[#10402E] dark:bg-[#173026] dark:text-[#4FD085]">
              {f.icon}
            </div>
            <h3 className="text-lg font-bold">{f.title}</h3>
            <p className="mt-1.5 text-sm text-[#4B6358] dark:text-[#9EB7AB]">{f.desc}</p>
          </Card>
        ))}
      </div>

      <div className="text-center">
        <Link to="/register">
          <Button size="lg">Start Using FarmWise</Button>
        </Link>
      </div>
    </div>
  );
}
