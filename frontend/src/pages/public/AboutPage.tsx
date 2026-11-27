import { Link } from "react-router-dom";
import { Award, Compass, HeartHandshake, ShieldCheck } from "lucide-react";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";

export function AboutPage() {
  return (
    <div className="mx-auto max-w-6xl space-y-12 px-4 py-14 sm:px-6 lg:px-8">
      <div className="max-w-3xl space-y-4">
        <Badge tone="green">About FarmWise</Badge>
        <h1 className="font-display text-4xl font-bold tracking-tight sm:text-5xl">
          Empowering Indian agriculture with clarity, science, and trusted commerce
        </h1>
        <p className="text-base leading-relaxed text-[#4B6358] dark:text-[#9EB7AB]">
          FarmWise was architected to eliminate fragmented spreadsheets, opaque input sourcing, and scattered field logs.
          Our mission is to give every farmer, FPO, agronomist, input supplier, and buyer a unified digital workspace
          anchored in Indian farming realities, INR (₹) accounting, and localization-ready architecture.
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {[
          {
            icon: <Compass className="h-5 w-5" />,
            title: "Our Vision",
            desc: "Smarter Farming. Better Harvests. Stronger Futures — connecting field-level telemetry, soil stewardship, and fair market discovery.",
          },
          {
            icon: <ShieldCheck className="h-5 w-5" />,
            title: "Data Integrity First",
            desc: "We never fabricate weather data, market prices, or expert credentials. Every third-party integration reports its exact verification and live status.",
          },
          {
            icon: <HeartHandshake className="h-5 w-5" />,
            title: "Multi-Stakeholder Ecosystem",
            desc: "Dedicated portals for Farmers, Agricultural Experts, Input Suppliers, Produce Buyers, and Platform Administrators.",
          },
          {
            icon: <Award className="h-5 w-5" />,
            title: "Ready for Scale",
            desc: "Built on PostgreSQL, Flyway, Java 21 Spring Boot, and React TypeScript — designed to extend seamlessly into regional Indian languages and international markets.",
          },
        ].map((item) => (
          <Card key={item.title}>
            <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-[#EEF3EE] text-[#10402E] dark:bg-[#173026] dark:text-[#4FD085]">
              {item.icon}
            </div>
            <h2 className="text-xl font-bold">{item.title}</h2>
            <p className="mt-2 text-sm leading-relaxed text-[#4B6358] dark:text-[#9EB7AB]">{item.desc}</p>
          </Card>
        ))}
      </div>

      <div className="flex flex-wrap gap-3">
        <Link to="/register">
          <Button>Join FarmWise</Button>
        </Link>
        <Link to="/contact">
          <Button variant="outline">Contact Our Team</Button>
        </Link>
      </div>
    </div>
  );
}
