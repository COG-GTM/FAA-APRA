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

import gov.faa.ait.apra.security.AuthenticationFilter;
import gov.faa.ait.apra.security.AuditRequestFilter;
import gov.faa.ait.apra.security.GlobalExceptionMapper;
import gov.faa.ait.apra.security.InputValidationFilter;
import gov.faa.ait.apra.security.SecurityHeadersFilter;
import gov.faa.ait.apra.security.TLSConfig;
import io.swagger.jaxrs.config.BeanConfig;

/**
 * This is the base application for CIFP and represents the JAX-RS application for scanning of REST services through the classes that are specified.
 * @author FAA
 *
 */
public class DownloadServiceApp extends Application {
	private static final Logger logger = LoggerFactory.getLogger(DownloadServiceApp.class);

	public DownloadServiceApp () {
		TLSConfig.enforceMinimumTLS();

		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setTitle("FAA Aeronautic Product Release API");
		beanConfig.setVersion("1.1.0");
		beanConfig.setSchemes(new String [] {"https"});
		beanConfig.setHost("soa.smext.faa.gov");
		beanConfig.setBasePath("/apra");
		beanConfig.setResourcePackage("io.swagger.resources");
		beanConfig.setLicense("US Public Domain");
		beanConfig.setLicenseUrl("http://www.usa.gov/publicdomain/label/1.0/");
		beanConfig.setScan(true);

		logger.info("APRA initialized with STIG security controls enabled");
	}

	@Override
	public Set<Class<?>> getClasses() {
		if (logger.isDebugEnabled())
			logger.debug("Resource classes being retrieved from AirportStatus jersey application.");
		Set<Class<?>> s = new HashSet <>();
		s.add(gov.faa.ait.apra.api.CIFP.class);
		s.add(gov.faa.ait.apra.api.ProductApiListener.class);
		s.add(io.swagger.jaxrs.listing.ApiListingResource.class);
		s.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
		
		//Manually adding MOXyJSONFeature
        s.add(org.glassfish.jersey.moxy.json.MoxyJsonFeature.class);

        // STIG V-220641: Security headers on all responses
        s.add(SecurityHeadersFilter.class);
        // STIG V-220631/V-220632: Input validation and sanitization
        s.add(InputValidationFilter.class);
        // STIG V-220635: Audit logging on all requests
        s.add(AuditRequestFilter.class);
        // STIG V-220629: Authentication for management endpoints
        s.add(AuthenticationFilter.class);
        // STIG V-220641: Generic error handling
        s.add(GlobalExceptionMapper.class);

		return s;
	}
}
