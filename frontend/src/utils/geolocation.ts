export interface GeolocationResult {
  city: string;
  district?: string;
  state: string;
  country?: string;
  postalCode?: string;
  latitude: number;
  longitude: number;
}

export type GeolocationStatus =
  | "idle"
  | "locating"
  | "geocoding"
  | "success"
  | "permission_denied"
  | "position_unavailable"
  | "timeout"
  | "unsupported"
  | "error";

export interface GeolocationState {
  status: GeolocationStatus;
  message?: string;
  result?: GeolocationResult;
}

export interface DetectLocationOptions {
  timeoutMs?: number;
  onStatusChange?: (status: GeolocationStatus, message?: string) => void;
  fetchFn?: typeof fetch;
}

/**
 * Reverse-geocode coordinates using BigDataCloud's free client-side API.
 */
export async function reverseGeocodeWithBigDataCloud(
  lat: number,
  lng: number,
  signal?: AbortSignal,
  customFetch: typeof fetch = fetch,
): Promise<GeolocationResult | null> {
  try {
    const res = await customFetch(
      `https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${lat}&longitude=${lng}&localityLanguage=en`,
      { signal },
    );
    if (!res.ok) return null;
    const data = await res.json();

    let district: string | undefined;
    if (Array.isArray(data.localityInfo?.administrative)) {
      const distObj = data.localityInfo.administrative.find(
        (item: { adminLevel?: number; description?: string; name?: string }) =>
          item.adminLevel === 5 ||
          (item.description && item.description.toLowerCase().includes("district")),
      );
      if (distObj?.name) {
        district = distObj.name.replace(/\s*district$/i, "").trim();
      }
    }

    const city =
      data.locality?.trim() ||
      data.city?.trim() ||
      district ||
      "";

    const state = data.principalSubdivision?.trim() || "";
    const country = data.countryName?.trim() || "";
    const postalCode = data.postcode?.trim() || undefined;

    if (!city && !state) return null;

    return {
      city,
      district,
      state,
      country,
      postalCode,
      latitude: lat,
      longitude: lng,
    };
  } catch {
    return null;
  }
}

/**
 * Reverse-geocode coordinates using OpenStreetMap Nominatim API as fallback.
 */
export async function reverseGeocodeWithNominatim(
  lat: number,
  lng: number,
  signal?: AbortSignal,
  customFetch: typeof fetch = fetch,
): Promise<GeolocationResult | null> {
  try {
    const res = await customFetch(
      `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`,
      {
        headers: { Accept: "application/json" },
        signal,
      },
    );
    if (!res.ok) return null;
    const data = await res.json();
    const addr = data.address || {};

    const city =
      addr.village?.trim() ||
      addr.town?.trim() ||
      addr.city?.trim() ||
      addr.suburb?.trim() ||
      addr.hamlet?.trim() ||
      addr.county?.trim() ||
      "";

    const district =
      addr.state_district?.replace(/\s*district$/i, "").trim() ||
      addr.county?.trim() ||
      undefined;

    const state = addr.state?.trim() || "";
    const country = addr.country?.trim() || "";
    const postalCode = addr.postcode?.trim() || undefined;

    if (!city && !state) return null;

    return {
      city,
      district,
      state,
      country,
      postalCode,
      latitude: lat,
      longitude: lng,
    };
  } catch {
    return null;
  }
}

/**
 * Requests device coordinates via navigator.geolocation and reverse-geocodes
 * them into structured address information (city, district, state, country, postal code).
 */
export async function detectCurrentLocation(
  options?: DetectLocationOptions,
): Promise<GeolocationResult> {
  const timeoutMs = options?.timeoutMs ?? 10000;
  const customFetch = options?.fetchFn ?? fetch;

  if (typeof window === "undefined" || !navigator?.geolocation) {
    const message = "Geolocation is not supported by your browser. Please enter your location manually.";
    options?.onStatusChange?.("unsupported", message);
    const err = new Error(message) as Error & { code: GeolocationStatus };
    err.code = "unsupported";
    throw err;
  }

  options?.onStatusChange?.("locating", "Requesting device location...");

  const coords = await new Promise<GeolocationCoordinates>((resolve, reject) => {
    navigator.geolocation.getCurrentPosition(
      (pos) => resolve(pos.coords),
      (err) => reject(err),
      {
        enableHighAccuracy: false,
        timeout: timeoutMs,
        maximumAge: 60000,
      },
    );
  }).catch((err: GeolocationPositionError) => {
    let status: GeolocationStatus = "error";
    let message = "Unable to retrieve device location. You can enter your City and State manually.";

    if (err.code === 1) {
      status = "permission_denied";
      message = "Location access was denied. You can enter your City and State manually.";
    } else if (err.code === 2) {
      status = "position_unavailable";
      message = "Location information is unavailable. Please enter your City and State manually.";
    } else if (err.code === 3) {
      status = "timeout";
      message = "Location detection timed out. Click retry or enter your City and State manually.";
    }

    options?.onStatusChange?.(status, message);
    const customError = new Error(message) as Error & { code: GeolocationStatus };
    customError.code = status;
    throw customError;
  });

  options?.onStatusChange?.("geocoding", "Identifying city, district, and state...");

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 7000);

  try {
    let result = await reverseGeocodeWithBigDataCloud(
      coords.latitude,
      coords.longitude,
      controller.signal,
      customFetch,
    );

    if (!result) {
      result = await reverseGeocodeWithNominatim(
        coords.latitude,
        coords.longitude,
        controller.signal,
        customFetch,
      );
    }

    if (!result) {
      const message = "Coordinates detected, but address details could not be resolved. Please enter City and State manually.";
      options?.onStatusChange?.("error", message);
      const customError = new Error(message) as Error & { code: GeolocationStatus };
      customError.code = "error";
      throw customError;
    }

    const summary = [result.city, result.state, result.country].filter(Boolean).join(", ");
    options?.onStatusChange?.("success", `Detected: ${summary}`);
    return result;
  } finally {
    clearTimeout(timer);
  }
}
