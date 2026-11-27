package com.farmwise.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI farmWiseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FarmWise Backend API")
                        .version("v1")
                        .description("Core Spring Boot API foundation for the FarmWise agricultural ecosystem.")
                        .contact(new Contact().name("FarmWise Engineering")))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT authentication foundation (tokens issued by auth module in upcoming steps).")));
    }
}
