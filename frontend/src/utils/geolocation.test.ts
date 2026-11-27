import { describe, expect, it, vi, beforeEach, afterEach } from "vitest";
import {
  detectCurrentLocation,
  reverseGeocodeWithBigDataCloud,
  reverseGeocodeWithNominatim,
  type GeolocationStatus,
} from "./geolocation";

describe("Geolocation & Reverse Geocoding Utility", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  describe("reverseGeocodeWithBigDataCloud", () => {
    it("successfully extracts city, district, state, country, and postal code", async () => {
      const mockResponse = {
        city: "Guntur",
        locality: "Guntur",
        principalSubdivision: "Andhra Pradesh",
        countryName: "India",
        postcode: "522001",
        localityInfo: {
          administrative: [
            { adminLevel: 2, name: "India" },
            { adminLevel: 4, name: "Andhra Pradesh" },
            { adminLevel: 5, name: "Guntur district" },
          ],
        },
      };

      const mockFetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => mockResponse,
      });

      const res = await reverseGeocodeWithBigDataCloud(16.3067, 80.4365, undefined, mockFetch as unknown as typeof fetch);
      expect(res).not.toBeNull();
      expect(res?.city).toBe("Guntur");
      expect(res?.district).toBe("Guntur");
      expect(res?.state).toBe("Andhra Pradesh");
      expect(res?.country).toBe("India");
      expect(res?.postalCode).toBe("522001");
      expect(res?.latitude).toBe(16.3067);
      expect(res?.longitude).toBe(80.4365);
    });

    it("returns null if response is not ok", async () => {
      const mockFetch = vi.fn().mockResolvedValue({
        ok: false,
        status: 500,
      });

      const res = await reverseGeocodeWithBigDataCloud(16.3067, 80.4365, undefined, mockFetch as unknown as typeof fetch);
      expect(res).toBeNull();
    });
  });

  describe("reverseGeocodeWithNominatim", () => {
    it("successfully parses village and state district", async () => {
      const mockResponse = {
        address: {
          village: "Brodipet",
          state_district: "Guntur District",
          state: "Andhra Pradesh",
          country: "India",
          postcode: "522002",
        },
      };

      const mockFetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => mockResponse,
      });

      const res = await reverseGeocodeWithNominatim(16.3067, 80.4365, undefined, mockFetch as unknown as typeof fetch);
      expect(res).not.toBeNull();
      expect(res?.city).toBe("Brodipet");
      expect(res?.district).toBe("Guntur");
      expect(res?.state).toBe("Andhra Pradesh");
      expect(res?.country).toBe("India");
      expect(res?.postalCode).toBe("522002");
    });
  });

  describe("detectCurrentLocation", () => {
    afterEach(() => {
      vi.unstubAllGlobals();
    });

    it("handles permission denied error (code 1)", async () => {
      const mockGeolocation = {
        getCurrentPosition: vi.fn((_success, error) => {
          error({
            code: 1, // PERMISSION_DENIED
            message: "User denied Geolocation",
          });
        }),
      };

      vi.stubGlobal("navigator", { geolocation: mockGeolocation });

      const statusLog: GeolocationStatus[] = [];
      await expect(
        detectCurrentLocation({
          onStatusChange: (status) => statusLog.push(status),
        }),
      ).rejects.toThrow("Location access was denied");

      expect(statusLog).toContain("permission_denied");
    });

    it("handles timeout error (code 3)", async () => {
      const mockGeolocation = {
        getCurrentPosition: vi.fn((_success, error) => {
          error({
            code: 3, // TIMEOUT
            message: "Geolocation timeout",
          });
        }),
      };

      vi.stubGlobal("navigator", { geolocation: mockGeolocation });

      const statusLog: GeolocationStatus[] = [];
      await expect(
        detectCurrentLocation({
          onStatusChange: (status) => statusLog.push(status),
        }),
      ).rejects.toThrow("Location detection timed out");

      expect(statusLog).toContain("timeout");
    });

    it("handles position unavailable error (code 2)", async () => {
      const mockGeolocation = {
        getCurrentPosition: vi.fn((_success, error) => {
          error({
            code: 2, // POSITION_UNAVAILABLE
            message: "Position unavailable",
          });
        }),
      };

      vi.stubGlobal("navigator", { geolocation: mockGeolocation });

      const statusLog: GeolocationStatus[] = [];
      await expect(
        detectCurrentLocation({
          onStatusChange: (status) => statusLog.push(status),
        }),
      ).rejects.toThrow("Location information is unavailable");

      expect(statusLog).toContain("position_unavailable");
    });

    it("handles browser without geolocation support", async () => {
      vi.stubGlobal("navigator", {});

      const statusLog: GeolocationStatus[] = [];
      await expect(
        detectCurrentLocation({
          onStatusChange: (status) => statusLog.push(status),
        }),
      ).rejects.toThrow("Geolocation is not supported");

      expect(statusLog).toContain("unsupported");
    });

    it("successfully detects coordinates and populates address", async () => {
      const mockGeolocation = {
        getCurrentPosition: vi.fn((success) => {
          success({
            coords: {
              latitude: 28.6139,
              longitude: 77.209,
              accuracy: 50,
            },
          });
        }),
      };

      vi.stubGlobal("navigator", { geolocation: mockGeolocation });

      const mockFetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => ({
          city: "New Delhi",
          principalSubdivision: "Delhi",
          countryName: "India",
        }),
      });

      const statusLog: GeolocationStatus[] = [];
      const result = await detectCurrentLocation({
        onStatusChange: (status) => statusLog.push(status),
        fetchFn: mockFetch as unknown as typeof fetch,
      });

      expect(result.city).toBe("New Delhi");
      expect(result.state).toBe("Delhi");
      expect(result.country).toBe("India");
      expect(statusLog).toContain("locating");
      expect(statusLog).toContain("geocoding");
      expect(statusLog).toContain("success");
    });
  });
});
