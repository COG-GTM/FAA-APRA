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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gov.faa.ait.apra.cycle.ChartCycleData;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.util.ChartInfoTable;
import gov.faa.ait.apra.util.ChartInfoTableKey;

public class ChartInfoTableTest {

	@Test
	public void defaultConstructorCreatesEmptyTable() {
		ChartInfoTable table = new ChartInfoTable();
		assertTrue(table.isEmpty());
	}

	@Test
	public void constructorWithChartCycleDataLoadsElements() {
		ChartCycleElementsJson elem = new ChartCycleElementsJson();
		elem.setChart_city_name("Albuquerque");
		elem.setChart_cycle_period_code("Current");
		elem.setChart_cycle_type_code("Sectional");

		ChartCycleData data = new ChartCycleData("test", new ChartCycleElementsJson[] { elem });
		ChartInfoTable table = new ChartInfoTable(data);

		assertEquals(1, table.size());
	}

	@Test
	public void loadPopulatesTableWithUpperCaseKeys() {
		ChartCycleElementsJson elem = new ChartCycleElementsJson();
		elem.setChart_city_name("denver");
		elem.setChart_cycle_period_code("current");
		elem.setChart_cycle_type_code("tac");

		ChartCycleData data = new ChartCycleData("test", new ChartCycleElementsJson[] { elem });
		ChartInfoTable table = new ChartInfoTable();
		table.load(data);

		ChartInfoTableKey key = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		assertNotNull(table.get(key));
	}

	@Test
	public void loadMultipleElements() {
		ChartCycleElementsJson elem1 = new ChartCycleElementsJson();
		elem1.setChart_city_name("Denver");
		elem1.setChart_cycle_period_code("Current");
		elem1.setChart_cycle_type_code("TAC");

		ChartCycleElementsJson elem2 = new ChartCycleElementsJson();
		elem2.setChart_city_name("Seattle");
		elem2.setChart_cycle_period_code("Next");
		elem2.setChart_cycle_type_code("Sectional");

		ChartCycleData data = new ChartCycleData("test",
				new ChartCycleElementsJson[] { elem1, elem2 });
		ChartInfoTable table = new ChartInfoTable(data);

		assertEquals(2, table.size());
	}

	@Test
	public void lookupReturnsCorrectElement() {
		ChartCycleElementsJson elem = new ChartCycleElementsJson();
		elem.setChart_city_name("Albuquerque");
		elem.setChart_cycle_period_code("Current");
		elem.setChart_cycle_type_code("Sectional");
		elem.setChart_cycle_number("42");

		ChartCycleData data = new ChartCycleData("test", new ChartCycleElementsJson[] { elem });
		ChartInfoTable table = new ChartInfoTable(data);

		ChartInfoTableKey key = new ChartInfoTableKey("ALBUQUERQUE", "CURRENT", "SECTIONAL");
		ChartCycleElementsJson result = table.get(key);
		assertNotNull(result);
		assertEquals("42", result.getChart_cycle_number());
	}

	@Test
	public void lookupMissingKeyReturnsNull() {
		ChartInfoTable table = new ChartInfoTable();
		ChartInfoTableKey key = new ChartInfoTableKey("NONEXISTENT", "CURRENT", "TAC");
		assertNull(table.get(key));
	}
}
