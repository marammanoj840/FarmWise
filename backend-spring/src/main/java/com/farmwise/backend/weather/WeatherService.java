package com.farmwise.backend.weather;

import com.farmwise.backend.farm.FarmEntity;
import com.farmwise.backend.farm.FarmLocationEntity;
import com.farmwise.backend.farm.FarmLocationRepository;
import com.farmwise.backend.farm.FarmService;
import com.farmwise.backend.weather.WeatherDtos.WeatherResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeatherService {

    private final WeatherProvider weatherProvider;
    private final WeatherRecordRepository weatherRecordRepository;
    private final FarmService farmService;
    private final FarmLocationRepository farmLocationRepository;

    public WeatherService(
            WeatherProvider weatherProvider,
            WeatherRecordRepository weatherRecordRepository,
            FarmService farmService,
            FarmLocationRepository farmLocationRepository) {
        this.weatherProvider = weatherProvider;
        this.weatherRecordRepository = weatherRecordRepository;
        this.farmService = farmService;
        this.farmLocationRepository = farmLocationRepository;
    }

    @Transactional
    public WeatherResponse getFarmWeather(UUID farmId) {
        FarmEntity farm = farmService.requireOwnedFarm(farmId);
        FarmLocationEntity loc = farmLocationRepository.findByFarmId(farmId).orElse(null);
        String queryLocation = loc != null
                ? loc.getDistrict() + ", " + loc.getState() + ", " + loc.getCountry()
                : farm.getName();
        Double lat = loc != null ? loc.getLatitude() : null;
        Double lon = loc != null ? loc.getLongitude() : null;

        WeatherResponse response = weatherProvider.fetchCurrentAndForecast(farmId, queryLocation, lat, lon);
        if (response.liveDataAvailable()) {
            WeatherRecordEntity record = new WeatherRecordEntity();
            record.setFarmId(farmId);
            record.setLocationName(response.locationName());
            record.setLatitude(response.latitude());
            record.setLongitude(response.longitude());
            record.setTemperatureC(response.temperatureC());
            record.setHumidityPct(response.humidityPct());
            record.setRainfallMm(response.rainfallMm());
            record.setWindSpeedKmh(response.windSpeedKmh());
            record.setConditionSummary(response.conditionSummary());
            record.setAlertLevel(response.alertLevel());
            record.setAgronomicAdvice(String.join(" | ", response.recommendations()));
            record.setProviderName(response.providerName());
            record.setLiveData(true);
            record.setObservedAt(response.observedAt());
            weatherRecordRepository.save(record);
        }
        return response;
    }

    public WeatherResponse lookupLocationWeather(String location, Double latitude, Double longitude) {
        return weatherProvider.fetchCurrentAndForecast(null, location, latitude, longitude);
    }

    @Transactional(readOnly = true)
    public List<WeatherRecordEntity> getFarmWeatherHistory(UUID farmId) {
        farmService.requireOwnedFarm(farmId);
        return weatherRecordRepository.findTop10ByFarmIdOrderByObservedAtDesc(farmId);
    }
}
