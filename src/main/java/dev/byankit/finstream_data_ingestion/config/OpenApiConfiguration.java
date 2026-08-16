package dev.byankit.finstream_data_ingestion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finstream Data Ingestion API")
                        .version("1.0.0")
                        .description("REST API documentation for financial statement file uploads, metadata management, and status processing updates.")
                        .contact(new Contact()
                                .name("Finstream Engineering")
                                .email("support@finstream.dev")));
    }
}
