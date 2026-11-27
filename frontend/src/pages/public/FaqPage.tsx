import { useState } from "react";
import { ChevronDown } from "lucide-react";
import { Badge, Card } from "@/components/ui/Card";

const FAQS = [
  {
    q: "Is FarmWise free for individual Indian farmers?",
    a: "Yes. Farmers can register, manage multiple farms, log crops, cultivation tasks, soil tests, inventory, and INR finances at no subscription cost.",
  },
  {
    q: "How does FarmWise source weather forecasts?",
    a: "FarmWise integrates with Open-Meteo using your farm's district or GPS coordinates. If the external weather API is unreachable or disabled, FarmWise clearly reports that live data is unavailable rather than fabricating temperatures or rainfall.",
  },
  {
    q: "Are mandi market prices live?",
    a: "FarmWise includes an integration layer prepared for Agmarknet / eNAM credentials. When live credentials are not configured, the Market Price module explicitly labels records as SAMPLE reference data.",
  },
  {
    q: "Does the AI Agricultural Assistant require external credentials?",
    a: "Yes for generative LLM responses. When AI_PROVIDER_API_KEY is not configured, FarmWise transparently switches to non-AI agronomic reference mode using your authorized farm/crop records and our Crop Knowledge Base.",
  },
  {
    q: "How do I know an Agricultural Expert is genuinely verified?",
    a: "Experts start with a PENDING verification status upon registration. Verified badges appear only after a platform Administrator explicitly reviews and approves their credentials.",
  },
  {
    q: "Are online payments processed on marketplace orders?",
    a: "Online card/UPI capture is intentionally disabled until live payment gateway credentials are configured via the PaymentProvider abstraction. Orders record cleanly in PENDING_PAYMENT_CONFIG status without fake payment simulation.",
  },
];

export function FaqPage() {
  const [openIndex, setOpenIndex] = useState<number | null>(0);

  return (
    <div className="mx-auto max-w-4xl space-y-8 px-4 py-14 sm:px-6 lg:px-8">
      <div className="space-y-3">
        <Badge tone="green">Help Center</Badge>
        <h1 className="font-display text-4xl font-bold">Frequently Asked Questions</h1>
        <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
          Clear answers about FarmWise features, data honesty, security, and integrations.
        </p>
      </div>

      <div className="space-y-3">
        {FAQS.map((item, idx) => {
          const isOpen = openIndex === idx;
          return (
            <Card key={item.q} padding="p-0" className="overflow-hidden">
              <button
                type="button"
                onClick={() => setOpenIndex(isOpen ? null : idx)}
                aria-expanded={isOpen}
                className="flex w-full items-center justify-between gap-4 px-5 py-4 text-left font-semibold hover:bg-[#F7FAF7] dark:hover:bg-[#132920]"
              >
                <span>{item.q}</span>
                <ChevronDown className={`h-5 w-5 shrink-0 transition-transform ${isOpen ? "rotate-180" : ""}`} />
              </button>
              {isOpen && (
                <div className="border-t border-[#DCE5DC] px-5 py-4 text-sm leading-relaxed text-[#4B6358] dark:border-[#203D31] dark:text-[#9EB7AB]">
                  {item.a}
                </div>
              )}
            </Card>
          );
        })}
      </div>
    </div>
  );
}
