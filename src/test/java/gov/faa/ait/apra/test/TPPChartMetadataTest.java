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

import gov.faa.ait.apra.json.TPPChartMetadata;
import gov.faa.ait.apra.json.TPPMetadata;

public class TPPChartMetadataTest {

	@Test
	public void defaultConstructorInitializesFields() {
		TPPChartMetadata chart = new TPPChartMetadata();
		assertEquals("", chart.getName());
		assertNotNull(chart.getElements());
		assertEquals(1, chart.getElements().length);
	}

	@Test
	public void setAndGetName() {
		TPPChartMetadata chart = new TPPChartMetadata();
		chart.setName("TPPData");
		assertEquals("TPPData", chart.getName());
	}

	@Test
	public void setAndGetElements() {
		TPPChartMetadata chart = new TPPChartMetadata();
		TPPMetadata[] elements = new TPPMetadata[2];
		elements[0] = new TPPMetadata();
		elements[0].setVolume("NE-1");
		elements[1] = new TPPMetadata();
		elements[1].setVolume("SE-2");
		chart.setElements(elements);

		TPPMetadata[] returned = chart.getElements();
		assertEquals(2, returned.length);
		assertEquals("NE-1", returned[0].getVolume());
		assertEquals("SE-2", returned[1].getVolume());
	}

	@Test
	public void getElementsReturnsDefensiveCopy() {
		TPPChartMetadata chart = new TPPChartMetadata();
		TPPMetadata[] elements = new TPPMetadata[] { new TPPMetadata() };
		chart.setElements(elements);
		assertNotSame(elements, chart.getElements());
	}
}
