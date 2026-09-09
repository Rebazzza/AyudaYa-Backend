package com.donaciones.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ayudaYaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AyudaYa API")
                        .description("API REST para el sistema de gestión de donaciones de emergencia AyudaYa. "
                                + "Conecta a Donantes, Trabajadores de centros de acopio y la Organización.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Equipo AyudaYa").email("contacto@ayudaya.pe"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

}