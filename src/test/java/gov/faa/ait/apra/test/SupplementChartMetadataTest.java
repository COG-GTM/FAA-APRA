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

import org.junit.Test;

import gov.faa.ait.apra.json.SupplementChartMetadata;
import gov.faa.ait.apra.json.SupplementMetadata;

public class SupplementChartMetadataTest {

	@Test
	public void defaultConstructorInitializesFields() {
		SupplementChartMetadata chart = new SupplementChartMetadata();
		assertEquals("", chart.getName());
		assertNotNull(chart.getElements());
		assertEquals(1, chart.getElements().length);
	}

	@Test
	public void setAndGetName() {
		SupplementChartMetadata chart = new SupplementChartMetadata();
		chart.setName("SupplementData");
		assertEquals("SupplementData", chart.getName());
	}

	@Test
	public void setAndGetElements() {
		SupplementChartMetadata chart = new SupplementChartMetadata();
		SupplementMetadata[] elements = new SupplementMetadata[2];
		elements[0] = new SupplementMetadata();
		elements[0].setState("CA");
		elements[1] = new SupplementMetadata();
		elements[1].setState("TX");
		chart.setElements(elements);

		SupplementMetadata[] returned = chart.getElements();
		assertEquals(2, returned.length);
		assertEquals("CA", returned[0].getState());
		assertEquals("TX", returned[1].getState());
	}

	@Test
	public void getElementsReturnsDefensiveCopy() {
		SupplementChartMetadata chart = new SupplementChartMetadata();
		SupplementMetadata[] elements = new SupplementMetadata[] { new SupplementMetadata() };
		chart.setElements(elements);
		assertNotSame(elements, chart.getElements());
	}
}
