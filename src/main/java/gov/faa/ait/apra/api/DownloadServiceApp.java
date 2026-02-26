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
package gov.faa.ait.apra.api;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * This is the base application for CIFP and represents the JAX-RS application for scanning of REST services through the classes that are specified.
 * @author FAA
 *
 */
@OpenAPIDefinition(
	info = @Info(
		title = "FAA Aeronautic Product Release API",
		version = "1.1.0",
		license = @License(
			name = "US Public Domain",
			url = "https://www.usa.gov/publicdomain/label/1.0/"
		)
	),
	servers = @Server(url = "https://soa.smext.faa.gov/apra")
)
public class DownloadServiceApp extends Application {
	private static final Logger logger = LoggerFactory.getLogger(DownloadServiceApp.class);

	@Override
	public Set<Class<?>> getClasses() {
		if (logger.isDebugEnabled())
			logger.debug("Resource classes being retrieved from AirportStatus jersey application.");
		Set<Class<?>> s = new HashSet <>();
		s.add(gov.faa.ait.apra.api.CIFP.class);
		s.add(gov.faa.ait.apra.api.ProductApiListener.class);
		s.add(io.swagger.v3.jaxrs2.integration.resources.OpenApiResource.class);

		s.add(org.glassfish.jersey.jackson.JacksonFeature.class);
        
		return s;
	}
}
