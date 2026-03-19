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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * Thread-safe singleton holder for the JAX-RS {@link Client}.
 * Reusing a single client avoids the connection-leak that occurs when
 * {@code ClientBuilder.newClient()} is called per request without closing.
 */
public final class JaxrsClientHolder {

	private static final Client CLIENT = ClientBuilder.newClient();

	private JaxrsClientHolder() { }

	public static Client getClient() {
		return CLIENT;
	}
}
