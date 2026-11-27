import { Link } from "react-router-dom";
import { Building2, Shield, ShoppingBag, Sprout, Store, Users } from "lucide-react";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";

const SOLUTIONS = [
  {
    icon: <Sprout className="h-5 w-5" />,
    audience: "For Farmers & FPOs",
    desc: "Digitize multiple farms, crop varieties, cultivation calendars, stock levels, INR expenses/revenues, soil lab tests, weather alerts, and produce listings.",
    cta: "/register?role=FARMER",
    ctaLabel: "Register as Farmer",
  },
  {
    icon: <Users className="h-5 w-5" />,
    audience: "For Agricultural Experts",
    desc: "Receive structured farmer consultation requests with crop & farm context, schedule advisory appointments, and earn admin-verified credentials.",
    cta: "/register?role=EXPERT",
    ctaLabel: "Join as Expert",
  },
  {
    icon: <Store className="h-5 w-5" />,
    audience: "For Agri Input Suppliers",
    desc: "Publish seeds, fertilizers, crop protection, drip irrigation kits, and equipment, manage stock, and track incoming order fulfillment.",
    cta: "/register?role=SUPPLIER",
    ctaLabel: "Register as Supplier",
  },
  {
    icon: <ShoppingBag className="h-5 w-5" />,
    audience: "For Produce Buyers & Markets",
    desc: "Discover quality-graded harvests directly from farms, submit price/quantity inquiries, and procure agricultural inputs with full order tracking.",
    cta: "/register?role=BUYER",
    ctaLabel: "Register as Buyer",
  },
  {
    icon: <Building2 className="h-5 w-5" />,
    audience: "For Agribusinesses & Cooperatives",
    desc: "Standardize multi-farm reporting with PDF/CSV exports, transparent market benchmarks, and API-first Spring Boot integrations.",
    cta: "/contact",
    ctaLabel: "Partner With Us",
  },
  {
    icon: <Shield className="h-5 w-5" />,
    audience: "For Platform Administrators",
    desc: "Govern user roles, verify experts and suppliers, moderate products & produce listings, inspect audit logs, and monitor system integrations.",
    cta: "/login",
    ctaLabel: "Admin Sign In",
  },
];

export function SolutionsPage() {
  return (
    <div className="mx-auto max-w-7xl space-y-10 px-4 py-14 sm:px-6 lg:px-8">
      <div className="max-w-3xl space-y-3">
        <Badge tone="green">Stakeholder Solutions</Badge>
        <h1 className="font-display text-4xl font-bold sm:text-5xl">Tailored Portals for Every Agricultural Role</h1>
        <p className="text-base text-[#4B6358] dark:text-[#9EB7AB]">
          One unified database and security layer powering distinct workflows for producers, advisors, suppliers, buyers,
          and administrators.
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {SOLUTIONS.map((s) => (
          <Card key={s.audience} className="flex flex-col justify-between">
            <div>
              <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-[#EEF3EE] text-[#10402E] dark:bg-[#173026] dark:text-[#4FD085]">
                {s.icon}
              </div>
              <h2 className="text-xl font-bold">{s.audience}</h2>
              <p className="mt-2 text-sm leading-relaxed text-[#4B6358] dark:text-[#9EB7AB]">{s.desc}</p>
            </div>
            <div className="mt-6">
              <Link to={s.cta}>
                <Button variant="outline" className="w-full">
                  {s.ctaLabel}
                </Button>
              </Link>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}
