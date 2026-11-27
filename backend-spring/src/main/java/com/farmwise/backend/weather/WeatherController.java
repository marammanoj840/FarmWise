package com.farmwise.backend.weather;

import com.farmwise.backend.weather.WeatherDtos.WeatherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weather")
@Tag(name = "Weather & Environment", description = "Open-Meteo weather integration, farm alerts, and agronomic advisories")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/farm/{farmId}")
    @Operation(summary = "Get current weather and 5-day forecast for an owned farm")
    public ResponseEntity<WeatherResponse> farmWeather(@PathVariable UUID farmId) {
        return ResponseEntity.ok(weatherService.getFarmWeather(farmId));
    }

    @GetMapping("/lookup")
    @Operation(summary = "Lookup weather by location name or coordinates")
    public ResponseEntity<WeatherResponse> lookup(
            @RequestParam(required = false, defaultValue = "Ludhiana, Punjab") String location,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        return ResponseEntity.ok(weatherService.lookupLocationWeather(location, latitude, longitude));
    }

    @GetMapping("/farm/{farmId}/history")
    @Operation(summary = "Get recorded weather snapshots for an owned farm")
    public ResponseEntity<List<WeatherRecordEntity>> history(@PathVariable UUID farmId) {
        return ResponseEntity.ok(weatherService.getFarmWeatherHistory(farmId));
    }
}
