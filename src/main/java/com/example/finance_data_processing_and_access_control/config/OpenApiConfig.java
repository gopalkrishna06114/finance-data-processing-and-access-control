package com.example.finance_data_processing_and_access_control.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customAPI(){
        return new OpenAPI().info(new Info()
                .title("Finance Data Processing and Access Control Backend")
                .version("1.0.0")
                .description("Backend API for finance data processing with role-based access control")
                .contact(new Contact().name("Gopal Krishna")));
    }
}
