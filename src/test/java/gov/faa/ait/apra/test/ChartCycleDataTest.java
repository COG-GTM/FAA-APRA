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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import gov.faa.ait.apra.cycle.ChartCycleData;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;

public class ChartCycleDataTest {

	@Test
	public void defaultConstructorInitializesFields() {
		ChartCycleData data = new ChartCycleData();
		assertEquals("", data.getName());
		assertNotNull(data.getElements());
		assertEquals(1, data.getElements().length);
	}

	@Test
	public void parameterizedConstructor() {
		ChartCycleElementsJson[] elements = new ChartCycleElementsJson[2];
		elements[0] = new ChartCycleElementsJson();
		elements[1] = new ChartCycleElementsJson();
		ChartCycleData data = new ChartCycleData("TestCycle", elements);
		assertEquals("TestCycle", data.getName());
		assertEquals(2, data.getElements().length);
	}

	@Test
	public void setAndGetName() {
		ChartCycleData data = new ChartCycleData();
		data.setName("VFR_28DAY");
		assertEquals("VFR_28DAY", data.getName());
	}

	@Test
	public void getElementsReturnsDefensiveCopy() {
		ChartCycleElementsJson[] elements = new ChartCycleElementsJson[1];
		elements[0] = new ChartCycleElementsJson();
		ChartCycleData data = new ChartCycleData("Test", elements);
		ChartCycleElementsJson[] returned = data.getElements();
		assertNotSame(elements, returned);
	}

	@Test
	public void setElementsStoresDefensiveCopy() {
		ChartCycleData data = new ChartCycleData();
		ChartCycleElementsJson[] elements = new ChartCycleElementsJson[3];
		elements[0] = new ChartCycleElementsJson();
		elements[1] = new ChartCycleElementsJson();
		elements[2] = new ChartCycleElementsJson();
		data.setElements(elements);
		assertEquals(3, data.getElements().length);
	}
}
