package com.example.challenge.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flight Booking System API")
                        .version("1.0")
                        .description("API documentation for the Flight Booking System")
                        .contact(new Contact()
                                .name("Cem Aktas")
                                .email("cemaktas@ymail.com")
                        ));
    }

}
