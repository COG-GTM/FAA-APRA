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
package gov.faa.ait.apra.util;

import java.text.SimpleDateFormat;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides singleton instances of JAX-RS Client and ObjectMapper to avoid
 * creating expensive, short-lived objects on every request. Both Client and
 * ObjectMapper are thread-safe once configured.
 * 
 * @author FAA
 */
public final class HttpClientProvider {

	private static final Client CLIENT = ClientBuilder.newClient();

	private static final ObjectMapper MAPPER;

	static {
		MAPPER = new ObjectMapper();
		MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
	}

	private HttpClientProvider() { }

	/**
	 * Returns a shared, thread-safe JAX-RS Client instance.
	 * @return the singleton Client
	 */
	public static Client getClient() {
		return CLIENT;
	}

	/**
	 * Returns a shared, thread-safe ObjectMapper pre-configured with
	 * FAIL_ON_UNKNOWN_PROPERTIES=false and date format yyyy-MM-dd.
	 * @return the singleton ObjectMapper
	 */
	public static ObjectMapper getObjectMapper() {
		return MAPPER;
	}
}
