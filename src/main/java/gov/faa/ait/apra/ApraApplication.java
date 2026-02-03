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
package gov.faa.ait.apra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@SpringBootApplication
@EnableCaching
@EnableAsync
@OpenAPIDefinition(
    info = @Info(
        title = "FAA Aeronautical Product Release API",
        version = "2.0.0",
        description = "RESTful web service providing automated, programmatic access to FAA aeronautical charts and aviation data products. " +
                      "Chart download URLs change with every release cycle (every 28 or 56 days), and this API provides current URLs.",
        license = @License(
            name = "US Public Domain (CC0 1.0)",
            url = "http://www.usa.gov/publicdomain/label/1.0/"
        ),
        contact = @Contact(
            name = "FAA Aeronautical Information Services",
            url = "https://www.faa.gov/air_traffic/flight_info/aeronav/"
        )
    ),
    servers = {
        @Server(url = "https://soa.smext.faa.gov/apra", description = "Production Server"),
        @Server(url = "http://localhost:8080", description = "Local Development Server")
    }
)
public class ApraApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApraApplication.class, args);
    }
}
