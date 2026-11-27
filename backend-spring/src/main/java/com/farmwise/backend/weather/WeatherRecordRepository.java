package com.farmwise.backend.weather;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRecordRepository extends JpaRepository<WeatherRecordEntity, UUID> {
    List<WeatherRecordEntity> findTop10ByFarmIdOrderByObservedAtDesc(UUID farmId);
}
