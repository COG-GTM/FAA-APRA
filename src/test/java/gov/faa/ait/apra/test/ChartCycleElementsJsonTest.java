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

import java.util.Date;

import org.junit.Test;

import gov.faa.ait.apra.cycle.ChartCycleElementsJson;

public class ChartCycleElementsJsonTest {

	@Test
	public void defaultConstructorFieldsAreNull() {
		ChartCycleElementsJson elem = new ChartCycleElementsJson();
		assertNull(elem.getChart_cycle_period_code());
		assertNull(elem.getChart_cycle_type_code());
		assertNull(elem.getChart_cycle_number());
		assertNull(elem.getQuery_date());
		assertNull(elem.getChart_city_name());
	}

	@Test
	public void getEffectiveDateReturnsCurrentTimeWhenNull() {
		ChartCycleElementsJson elem = new ChartCycleElementsJson();
		Date before = new Date(System.currentTimeMillis());
		Date effectiveDate = elem.getChart_effective_date();
		Date after = new Date(System.currentTimeMillis());
		assertNotNull(effectiveDate);
		// The date should be between before and after
		assertTrue(effectiveDate.getTime() >= before.getTime() - 1000);
		assertTrue(effectiveDate.getTime() <= after.getTime() + 1000);
	}

	private void assertTrue(boolean condition) {
		org.junit.Assert.assertTrue(condition);
	}

	@Test
	public void setAndGetPeriodCode() {
		ChartCycleElementsJson elem = new ChartCycleElementsJson();
		elem.setChart_cycle_period_code("Current");
		assertEquals("Current", elem.getChart_cycle_period_code());
	}

	@Test
	public void setAndGetTypeCode() {
		ChartCycleElementsJson elem = new ChartCycleElementsJson();
		elem.setChart_cycle_type_code("SECTIONAL");
		assertEquals("SECTIONAL", elem.getChart_cycle_type_code());
	}

	@Test
	public void setAndGetEffectiveDate() {
		ChartCycleElementsJson elem = new ChartCycleElementsJson();
		Date testDate = new Date(1000000L);
		elem.setChart_effective_date(testDate);
		assertEquals(testDate.getTime(), elem.getChart_effective_date().getTime());
	}

	@Test
	public void setEffectiveDateDefensiveCopy() {
		ChartCycleElementsJson elem = new ChartCycleElementsJson();
		Date testDate = new Date(1000000L);
		elem.setChart_effective_date(testDate);
		Date returned = elem.getChart_effective_date();
		assertNotSame(testDate, returned);
	}

	@Test
	public void setEffectiveDateNullIsIgnored() {
		ChartCycleElementsJson elem = new ChartCycleElementsJson();
		Date testDate = new Date(1000000L);
		elem.setChart_effective_date(testDate);
		elem.setChart_effective_date(null);
		assertEquals(testDate.getTime(), elem.getChart_effective_date().getTime());
	}

	@Test
	public void setAndGetCycleNumber() {
		ChartCycleElementsJson elem = new ChartCycleElementsJson();
		elem.setChart_cycle_number("42");
		assertEquals("42", elem.getChart_cycle_number());
	}

	@Test
	public void setAndGetQueryDate() {
		ChartCycleElementsJson elem = new ChartCycleElementsJson();
		elem.setQuery_date("2024-01-15");
		assertEquals("2024-01-15", elem.getQuery_date());
	}

	@Test
	public void setAndGetCityName() {
		ChartCycleElementsJson elem = new ChartCycleElementsJson();
		elem.setChart_city_name("ALBUQUERQUE");
		assertEquals("ALBUQUERQUE", elem.getChart_city_name());
	}
}
