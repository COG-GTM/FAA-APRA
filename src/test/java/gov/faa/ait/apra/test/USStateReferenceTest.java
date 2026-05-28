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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import gov.faa.ait.apra.json.USState;
import gov.faa.ait.apra.json.USStateReference;

public class USStateReferenceTest {

	@Test
	public void defaultConstructorInitializesFields() {
		USStateReference ref = new USStateReference();
		assertEquals("", ref.getName());
	}

	@Test
	public void setAndGetName() {
		USStateReference ref = new USStateReference();
		ref.setName("US_STATES");
		assertEquals("US_STATES", ref.getName());
	}

	@Test
	public void setAndGetElements() {
		USStateReference ref = new USStateReference();
		USState[] states = new USState[2];
		states[0] = new USState();
		states[0].setAbbreviation("CA");
		states[0].setName("California");
		states[1] = new USState();
		states[1].setAbbreviation("TX");
		states[1].setName("Texas");
		ref.setElements(states);

		USState[] returned = ref.getElements();
		assertNotNull(returned);
		assertEquals(2, returned.length);
		assertEquals("CA", returned[0].getAbbreviation());
		assertEquals("TX", returned[1].getAbbreviation());
	}

	@Test
	public void getElementsReturnsDefensiveCopy() {
		USStateReference ref = new USStateReference();
		USState[] states = new USState[] { new USState() };
		ref.setElements(states);
		USState[] copy = ref.getElements();
		assertNotSame(states, copy);
	}
}
