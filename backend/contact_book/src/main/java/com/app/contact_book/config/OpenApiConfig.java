package com.app.contact_book.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "BearerAuth";
    private static final String SCHEME = "Bearer";

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
            .info(new Info()
                .title("Contact Book API")
                .description("REST API for user and contact management. Implements JWT and OAuth2 authentication.")
                .version("1.0.0")
            )
            .components(new Components()
                .addSecuritySchemes(SCHEME, createSecurityScheme())
            )
            .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));
                
    }

    private SecurityScheme createSecurityScheme(){
        return new SecurityScheme()
            .name(SCHEME_NAME)
            .type(SecurityScheme.Type.HTTP)
            .scheme(SCHEME)
            .bearerFormat("JWT")
            .description("JWT authentication.  Enter 'Bearer' followed by the token.");
    }

}
