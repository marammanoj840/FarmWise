import { createContext, useContext, useState, type ReactNode } from "react";
import type { SupportedLocale } from "@/types/api";

const DICTIONARY: Record<SupportedLocale, Record<string, string>> = {
  en: {
    brandName: "FarmWise",
    tagline: "Smarter Farming. Better Harvests. Stronger Futures.",
    navHome: "Home",
    navAbout: "About",
    navFeatures: "Features",
    navHowItWorks: "How It Works",
    navSolutions: "Solutions",
    navResources: "Resources",
    navMarketplace: "Marketplace",
    navContact: "Contact",
    login: "Sign In",
    register: "Get Started",
    dashboard: "Dashboard",
  },
  hi: {
    brandName: "FarmWise",
    tagline: "स्मार्ट खेती. बेहतर फसल. मजबूत भविष्य.",
    navHome: "होम",
    navAbout: "हमारे बारे में",
    navFeatures: "विशेषताएं",
    navHowItWorks: "कैसे काम करता है",
    navSolutions: "समाधान",
    navResources: "किसान संसाधन",
    navMarketplace: "मार्केटप्लेस",
    navContact: "संपर्क",
    login: "लॉग इन",
    register: "शुरू करें",
    dashboard: "डैशबोर्ड",
  },
  pa: {
    brandName: "FarmWise",
    tagline: "Smarter Farming. Better Harvests. Stronger Futures.",
    navHome: "Home",
    navAbout: "About",
    navFeatures: "Features",
    navHowItWorks: "How It Works",
    navSolutions: "Solutions",
    navResources: "Resources",
    navMarketplace: "Marketplace",
    navContact: "Contact",
    login: "Sign In",
    register: "Get Started",
    dashboard: "Dashboard",
  },
  mr: {
    brandName: "FarmWise",
    tagline: "Smarter Farming. Better Harvests. Stronger Futures.",
    navHome: "Home",
    navAbout: "About",
    navFeatures: "Features",
    navHowItWorks: "How It Works",
    navSolutions: "Solutions",
    navResources: "Resources",
    navMarketplace: "Marketplace",
    navContact: "Contact",
    login: "Sign In",
    register: "Get Started",
    dashboard: "Dashboard",
  },
  ta: {
    brandName: "FarmWise",
    tagline: "Smarter Farming. Better Harvests. Stronger Futures.",
    navHome: "Home",
    navAbout: "About",
    navFeatures: "Features",
    navHowItWorks: "How It Works",
    navSolutions: "Solutions",
    navResources: "Resources",
    navMarketplace: "Marketplace",
    navContact: "Contact",
    login: "Sign In",
    register: "Get Started",
    dashboard: "Dashboard",
  },
};

interface LocaleContextValue {
  locale: SupportedLocale;
  setLocale: (loc: SupportedLocale) => void;
  t: (key: string) => string;
}

const LocaleContext = createContext<LocaleContextValue | null>(null);

export function LocaleProvider({ children }: { children: ReactNode }) {
  const [locale, setLocaleState] = useState<SupportedLocale>(() => {
    const saved = localStorage.getItem("fw_locale") as SupportedLocale | null;
    return saved && DICTIONARY[saved] ? saved : "en";
  });

  const setLocale = (loc: SupportedLocale) => {
    localStorage.setItem("fw_locale", loc);
    setLocaleState(loc);
  };

  const t = (key: string) => DICTIONARY[locale]?.[key] ?? DICTIONARY.en[key] ?? key;

  return <LocaleContext.Provider value={{ locale, setLocale, t }}>{children}</LocaleContext.Provider>;
}

export function useLocale() {
  const ctx = useContext(LocaleContext);
  if (!ctx) throw new Error("useLocale must be used inside LocaleProvider");
  return ctx;
}
