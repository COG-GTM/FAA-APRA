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
package gov.faa.ait.apra.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gov.faa.ait.apra.bootstrap.ErrorCodes;

public class ErrorCodesTest {

	@Test
	public void response200IsOK() {
		assertEquals("OK", ErrorCodes.RESPONSE_200);
	}

	@Test
	public void error400ContainsIllegalArguments() {
		assertNotNull(ErrorCodes.ERROR_400);
		assertTrue(ErrorCodes.ERROR_400.contains("Illegal arguments"));
	}

	@Test
	public void error404ContainsNotFound() {
		assertNotNull(ErrorCodes.ERROR_404);
		assertTrue(ErrorCodes.ERROR_404.contains("not been released")
				|| ErrorCodes.ERROR_404.contains("could not be found"));
	}

	@Test
	public void error500ContainsInternalError() {
		assertNotNull(ErrorCodes.ERROR_500);
		assertTrue(ErrorCodes.ERROR_500.contains("Internal service error"));
	}

	@Test
	public void deprecatedContainsDeprecated() {
		assertNotNull(ErrorCodes.DEPRECATED);
		assertTrue(ErrorCodes.DEPRECATED.contains("deprecated"));
	}
}
