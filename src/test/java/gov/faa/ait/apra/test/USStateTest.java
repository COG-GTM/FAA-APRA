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

import org.junit.Test;

import gov.faa.ait.apra.json.USState;

public class USStateTest {

	@Test
	public void defaultConstructorReturnsDC() {
		USState state = new USState();
		assertEquals("DC", state.getAbbreviation());
		assertEquals("District of Columbia", state.getName());
	}

	@Test
	public void setAndGetAbbreviation() {
		USState state = new USState();
		state.setAbbreviation("NE");
		assertEquals("NE", state.getAbbreviation());
	}

	@Test
	public void setAndGetName() {
		USState state = new USState();
		state.setName("Nebraska");
		assertEquals("Nebraska", state.getName());
	}

	@Test
	public void overrideDefaultValues() {
		USState state = new USState();
		state.setAbbreviation("CA");
		state.setName("California");
		assertEquals("CA", state.getAbbreviation());
		assertEquals("California", state.getName());
	}
}
