/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 * 
 * APRA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package gov.faa.ait.apra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apraOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FAA Aeronautical Product Release API")
                        .description("The Aeronautical Product Release API (APRA) provides programmatic access to FAA aeronautical charts and aviation data products.")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("FAA")
                                .url("https://www.faa.gov"))
                        .license(new License()
                                .name("US Public Domain")
                                .url("http://www.usa.gov/publicdomain/label/1.0/")))
                .servers(List.of(
                        new Server()
                                .url("https://soa.smext.faa.gov/apra")
                                .description("Production Server"),
                        new Server()
                                .url("http://localhost:8080/apra")
                                .description("Local Development Server")));
    }
}
