package com.farmwise.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FarmWiseBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmWiseBackendApplication.class, args);
    }
}
