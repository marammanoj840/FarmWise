package com.farmwise.backend.weather;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class WeatherDtos {

    private WeatherDtos() {
    }

    public record DailyForecastItem(
            String date,
            Double tempMaxC,
            Double tempMinC,
            Double precipitationMm,
            Integer precipitationProbabilityPct) {
    }

    public record WeatherResponse(
            UUID farmId,
            String locationName,
            Double latitude,
            Double longitude,
            boolean liveDataAvailable,
            String providerName,
            String statusMessage,
            Double temperatureC,
            Double humidityPct,
            Double rainfallMm,
            Double windSpeedKmh,
            String conditionSummary,
            String alertLevel,
            List<String> recommendations,
            List<DailyForecastItem> dailyForecast,
            OffsetDateTime observedAt) {
    }
}
