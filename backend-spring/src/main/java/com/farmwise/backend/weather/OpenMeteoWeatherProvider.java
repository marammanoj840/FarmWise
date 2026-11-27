package com.farmwise.backend.weather;

import com.farmwise.backend.config.IntegrationConfig.IntegrationProperties;
import com.farmwise.backend.weather.WeatherDtos.DailyForecastItem;
import com.farmwise.backend.weather.WeatherDtos.WeatherResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenMeteoWeatherProvider implements WeatherProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoWeatherProvider.class);

    private final RestClient restClient;
    private final IntegrationProperties properties;
    private final Clock utcClock;

    public OpenMeteoWeatherProvider(
            RestClient restClient,
            IntegrationProperties properties,
            Clock utcClock) {
        this.restClient = restClient;
        this.properties = properties;
        this.utcClock = utcClock;
    }

    @Override
    public String providerName() {
        return properties.getWeather().getProvider();
    }

    @Override
    @SuppressWarnings("unchecked")
    public WeatherResponse fetchCurrentAndForecast(
            UUID farmId, String locationName, Double latitude, Double longitude) {
        OffsetDateTime now = OffsetDateTime.now(utcClock);
        String configuredProvider = properties.getWeather().getProvider();
        if (configuredProvider == null
                || "disabled-in-test".equalsIgnoreCase(configuredProvider)
                || "none".equalsIgnoreCase(configuredProvider)) {
            return unavailable(
                    farmId,
                    locationName,
                    latitude,
                    longitude,
                    configuredProvider == null ? "none" : configuredProvider,
                    "Live weather provider is disabled or unconfigured in this environment.",
                    now);
        }

        try {
            Double resolvedLat = latitude;
            Double resolvedLon = longitude;
            String resolvedName = locationName != null && !locationName.isBlank() ? locationName.trim() : "India";

            if ((resolvedLat == null || resolvedLon == null) && !resolvedName.isBlank()) {
                String encoded = URLEncoder.encode(resolvedName, StandardCharsets.UTF_8);
                String geoUrl = properties.getWeather().getGeocodingUrl()
                        + "/search?name=" + encoded + "&count=1&language=en&format=json";
                Map<String, Object> geoRes = restClient.get().uri(geoUrl).retrieve().body(Map.class);
                if (geoRes != null && geoRes.get("results") instanceof List<?> results && !results.isEmpty()) {
                    Object first = results.get(0);
                    if (first instanceof Map<?, ?> m) {
                        resolvedLat = toDouble(m.get("latitude"));
                        resolvedLon = toDouble(m.get("longitude"));
                        Object nameObj = m.get("name");
                        Object adminObj = m.get("admin1");
                        if (nameObj != null) {
                            resolvedName = adminObj != null
                                    ? nameObj + ", " + adminObj
                                    : String.valueOf(nameObj);
                        }
                    }
                }
            }

            if (resolvedLat == null || resolvedLon == null) {
                return unavailable(
                        farmId,
                        resolvedName,
                        null,
                        null,
                        "open-meteo",
                        "Coordinates could not be resolved for '" + resolvedName
                                + "'. Provide latitude and longitude on the farm location.",
                        now);
            }

            String forecastUrl = properties.getWeather().getBaseUrl()
                    + "/forecast?latitude=" + resolvedLat
                    + "&longitude=" + resolvedLon
                    + "&current=temperature_2m,relative_humidity_2m,rain,wind_speed_10m,weather_code"
                    + "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max"
                    + "&timezone=auto&forecast_days=5";

            Map<String, Object> res = restClient.get().uri(forecastUrl).retrieve().body(Map.class);
            if (res == null || !(res.get("current") instanceof Map<?, ?> current)) {
                return unavailable(
                        farmId,
                        resolvedName,
                        resolvedLat,
                        resolvedLon,
                        "open-meteo",
                        "Live weather service returned an empty payload.",
                        now);
            }

            Double temp = toDouble(current.get("temperature_2m"));
            Double humidity = toDouble(current.get("relative_humidity_2m"));
            Double rain = toDouble(current.get("rain"));
            Double wind = toDouble(current.get("wind_speed_10m"));
            Integer code = toInt(current.get("weather_code"));
            String summary = describeWmoCode(code);

            List<DailyForecastItem> dailyItems = parseDaily(res.get("daily"));
            AdvisoryResult advisory = buildAgronomicAdvisories(temp, humidity, rain, wind, dailyItems);

            return new WeatherResponse(
                    farmId,
                    resolvedName,
                    resolvedLat,
                    resolvedLon,
                    true,
                    "open-meteo",
                    "Live weather retrieved from Open-Meteo.",
                    temp,
                    humidity,
                    rain,
                    wind,
                    summary,
                    advisory.alertLevel(),
                    advisory.recommendations(),
                    dailyItems,
                    now);
        } catch (Exception ex) {
            log.warn("Open-Meteo weather lookup unavailable for '{}': {}", locationName, ex.getMessage());
            return unavailable(
                    farmId,
                    locationName != null ? locationName : "Unknown",
                    latitude,
                    longitude,
                    "open-meteo",
                    "Live weather data is currently unavailable (" + ex.getClass().getSimpleName() + ").",
                    now);
        }
    }

    private WeatherResponse unavailable(
            UUID farmId,
            String locationName,
            Double lat,
            Double lon,
            String provider,
            String message,
            OffsetDateTime now) {
        return new WeatherResponse(
                farmId,
                locationName,
                lat,
                lon,
                false,
                provider,
                message,
                null,
                null,
                null,
                null,
                "Unavailable",
                "UNAVAILABLE",
                List.of("Live weather feed is unavailable; verify local conditions visually before spraying or irrigating."),
                List.of(),
                now);
    }

    private List<DailyForecastItem> parseDaily(Object dailyObj) {
        if (!(dailyObj instanceof Map<?, ?> daily)) {
            return List.of();
        }
        List<?> dates = daily.get("time") instanceof List<?> l ? l : List.of();
        List<?> tMax = daily.get("temperature_2m_max") instanceof List<?> l ? l : List.of();
        List<?> tMin = daily.get("temperature_2m_min") instanceof List<?> l ? l : List.of();
        List<?> precip = daily.get("precipitation_sum") instanceof List<?> l ? l : List.of();
        List<?> prob = daily.get("precipitation_probability_max") instanceof List<?> l ? l : List.of();

        List<DailyForecastItem> items = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            items.add(new DailyForecastItem(
                    String.valueOf(dates.get(i)),
                    i < tMax.size() ? toDouble(tMax.get(i)) : null,
                    i < tMin.size() ? toDouble(tMin.get(i)) : null,
                    i < precip.size() ? toDouble(precip.get(i)) : null,
                    i < prob.size() ? toInt(prob.get(i)) : null));
        }
        return items;
    }

    private record AdvisoryResult(String alertLevel, List<String> recommendations) {
    }

    private AdvisoryResult buildAgronomicAdvisories(
            Double temp, Double humidity, Double rain, Double wind, List<DailyForecastItem> daily) {
        List<String> recs = new ArrayList<>();
        String level = "NORMAL";

        if (wind != null && wind >= 20.0) {
            level = "ADVISORY";
            recs.add("Elevated wind speed (" + wind + " km/h): avoid foliar spraying to prevent drift.");
        }
        if (rain != null && rain >= 10.0) {
            level = "WARNING";
            recs.add("Active rainfall (" + rain + " mm): pause irrigation and inspect field drainage channels.");
        }
        if (temp != null && temp >= 38.0) {
            level = "WARNING";
            recs.add("High heat (" + temp + " °C): schedule irrigation during early morning or evening.");
        }
        if (humidity != null && humidity >= 85.0) {
            if ("NORMAL".equals(level)) {
                level = "ADVISORY";
            }
            recs.add("High relative humidity (" + humidity + "%): monitor susceptible crops for fungal blight or mildew.");
        }
        double upcomingRain = daily.stream()
                .map(DailyForecastItem::precipitationMm)
                .filter(v -> v != null)
                .mapToDouble(Double::doubleValue)
                .sum();
        if (upcomingRain >= 25.0) {
            recs.add(String.format("5-day forecast shows %.1f mm cumulative precipitation; adjust irrigation schedule accordingly.", upcomingRain));
        }
        if (recs.isEmpty()) {
            recs.add("Conditions are generally suitable for routine field scouting and scheduled operations.");
        }
        return new AdvisoryResult(level, recs);
    }

    private static String describeWmoCode(Integer code) {
        if (code == null) {
            return "Observed";
        }
        return switch (code) {
            case 0 -> "Clear sky";
            case 1, 2, 3 -> "Partly cloudy";
            case 45, 48 -> "Fog / Mist";
            case 51, 53, 55, 56, 57 -> "Drizzle";
            case 61, 63, 65, 66, 67, 80, 81, 82 -> "Rain";
            case 71, 73, 75, 77 -> "Snow";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Variable conditions (WMO " + code + ")";
        };
    }

    private static Double toDouble(Object o) {
        return o instanceof Number n ? n.doubleValue() : null;
    }

    private static Integer toInt(Object o) {
        return o instanceof Number n ? n.intValue() : null;
    }
}
