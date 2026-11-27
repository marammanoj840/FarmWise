package com.farmwise.backend.weather;

import com.farmwise.backend.weather.WeatherDtos.WeatherResponse;
import java.util.UUID;

public interface WeatherProvider {
    String providerName();

    WeatherResponse fetchCurrentAndForecast(UUID farmId, String locationName, Double latitude, Double longitude);
}
