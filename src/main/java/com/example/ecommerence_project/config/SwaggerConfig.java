package com.example.ecommerence_project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("🌸 Perfume eCommerce API")
                        .description("""
                                REST API for a perfume eCommerce platform.
                                
                                **How to authenticate:**
                                1. Call `POST /api/auth/login` with your credentials
                                2. Copy the `token` from the response
                                3. Click the **Authorize** button (🔒) at the top
                                4. Enter: `Bearer <your_token>`
                                
                                **Test accounts:**
                                - Customer: `customer@perfume.com` / `customer123`
                                - Admin: `admin@perfume.com` / `admin123`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Perfume Store Dev Team")
                                .email("dev@perfume.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste your JWT token here (without 'Bearer ' prefix)")));
    }
}
