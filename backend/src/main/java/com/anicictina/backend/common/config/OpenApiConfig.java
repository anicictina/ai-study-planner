package com.anicictina.backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
            .info(new Info()
                .title("AI Study Planner API")
                .description(
                    "REST API platforme za planiranje učenja uz primenu veštačke inteligencije. "
                        + "Većina endpoint-a zahteva JWT token dobijen preko /api/auth/login ili /api/auth/register "
                        + "— klikni 'Authorize' i unesi token da bi mogao/mogla da ih testiraš.")
                .version("v1"))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
            .components(new Components()
                .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                    .name(BEARER_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
