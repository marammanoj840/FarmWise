import { Badge, Card } from "@/components/ui/Card";

export function PrivacyPage() {
  return (
    <div className="mx-auto max-w-4xl space-y-6 px-4 py-14 sm:px-6 lg:px-8">
      <Badge tone="green">Legal & Privacy</Badge>
      <h1 className="font-display text-4xl font-bold">Privacy Policy</h1>
      <Card className="space-y-4 text-sm leading-relaxed text-[#355245] dark:text-[#B9CFC3]">
        <p>
          <strong>1. Ownership of Farm & Financial Data:</strong> FarmWise treats your farm holdings, crop records,
          soil tests, inventory logs, expenses, and income entries as strictly private data accessible only to your
          authenticated account (and authorized administrators for support/audit compliance).
        </p>
        <p>
          <strong>2. Authentication & Security:</strong> Passwords are hashed using BCrypt and never stored in plaintext.
          API communication uses signed JWT access tokens and rotatable refresh tokens stored as SHA-256 digests.
        </p>
        <p>
          <strong>3. Third-Party Integrations:</strong> Farm coordinates may be sent to Open-Meteo solely to retrieve
          local weather forecasts. Optional AI or market data providers receive context only when explicitly configured by
          the platform operator.
        </p>
        <p>
          <strong>4. Contact & Deletion:</strong> You may update profile information, revoke active sessions, or delete
          farm records directly inside the portal or contact support via the Contact page.
        </p>
      </Card>
    </div>
  );
}

export function TermsPage() {
  return (
    <div className="mx-auto max-w-4xl space-y-6 px-4 py-14 sm:px-6 lg:px-8">
      <Badge tone="green">Legal & Terms</Badge>
      <h1 className="font-display text-4xl font-bold">Terms and Conditions</h1>
      <Card className="space-y-4 text-sm leading-relaxed text-[#355245] dark:text-[#B9CFC3]">
        <p>
          <strong>1. Agricultural Advisory Disclaimer:</strong> Soil suitability indicators, crop knowledge base guides,
          and AI assistant replies are provided strictly for informational decision support. They do not constitute a
          certified laboratory diagnosis or replace qualified agricultural extension services.
        </p>
        <p>
          <strong>2. No Yield or Financial Guarantees:</strong> Profit/loss reports, expected yields, and sample market
          benchmarks reflect recorded entries and estimates only. FarmWise never guarantees harvest volume or financial
          returns.
        </p>
        <p>
          <strong>3. Marketplace & Produce Commerce:</strong> Suppliers and farmers are responsible for accuracy of
          product specifications, stock quantities, and produce quality grades. Payment gateway settlements activate only
          when a verified payment processor is configured.
        </p>
        <p>
          <strong>4. Acceptable Use:</strong> Users agree not to attempt unauthorized access to other farms' data,
          bypass role restrictions, or upload misleading product listings.
        </p>
      </Card>
    </div>
  );
}
