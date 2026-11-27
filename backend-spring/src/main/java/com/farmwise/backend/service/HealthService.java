package com.farmwise.backend.service;

import com.farmwise.backend.dto.HealthResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private static final Logger log = LoggerFactory.getLogger(HealthService.class);

    private final DataSource dataSource;
    private final Environment environment;
    private final Clock utcClock;
    private final String serviceName;
    private final String appVersion;

    public HealthService(
            DataSource dataSource,
            Environment environment,
            Clock utcClock,
            @Value("${spring.application.name:farmwise-backend}") String serviceName,
            @Value("${app.version:0.1.0-SNAPSHOT}") String appVersion) {
        this.dataSource = dataSource;
        this.environment = environment;
        this.utcClock = utcClock;
        this.serviceName = serviceName;
        this.appVersion = appVersion;
    }

    public HealthResponse checkHealth() {
        String dbStatus = checkDatabaseConnectivity();
        String overallStatus = "UP".equals(dbStatus) ? "UP" : "DEGRADED";
        List<String> profiles = Arrays.asList(environment.getActiveProfiles());
        return new HealthResponse(
                overallStatus,
                serviceName,
                appVersion,
                profiles,
                dbStatus,
                OffsetDateTime.now(utcClock));
    }

    private String checkDatabaseConnectivity() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1");
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next() && resultSet.getInt(1) == 1) {
                return "UP";
            }
            return "DOWN";
        } catch (Exception ex) {
            log.warn("Database connectivity probe failed: {}", ex.getMessage());
            return "DOWN";
        }
    }
}
