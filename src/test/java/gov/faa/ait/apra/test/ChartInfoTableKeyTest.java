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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gov.faa.ait.apra.util.ChartInfoTableKey;

public class ChartInfoTableKeyTest {

	@Test
	public void defaultConstructorFieldsAreNull() {
		ChartInfoTableKey key = new ChartInfoTableKey();
		assertNull(key.getCityRegion());
		assertNull(key.getPeriodCode());
		assertNull(key.getChartType());
	}

	@Test
	public void parameterizedConstructor() {
		ChartInfoTableKey key = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		assertEquals("DENVER", key.getCityRegion());
		assertEquals("CURRENT", key.getPeriodCode());
		assertEquals("TAC", key.getChartType());
	}

	@Test
	public void setAndGetCityRegion() {
		ChartInfoTableKey key = new ChartInfoTableKey();
		key.setCityRegion("SEATTLE");
		assertEquals("SEATTLE", key.getCityRegion());
	}

	@Test
	public void setAndGetPeriodCode() {
		ChartInfoTableKey key = new ChartInfoTableKey();
		key.setPeriodCode("NEXT");
		assertEquals("NEXT", key.getPeriodCode());
	}

	@Test
	public void setAndGetChartType() {
		ChartInfoTableKey key = new ChartInfoTableKey();
		key.setChartType("SECTIONAL");
		assertEquals("SECTIONAL", key.getChartType());
	}

	@Test
	public void toStringContainsAllFields() {
		ChartInfoTableKey key = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		String str = key.toString();
		assertTrue(str.contains("DENVER"));
		assertTrue(str.contains("CURRENT"));
		assertTrue(str.contains("TAC"));
	}

	@Test
	public void equalsSameObject() {
		ChartInfoTableKey key = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		assertTrue(key.equals(key));
	}

	@Test
	public void equalsIdenticalValues() {
		ChartInfoTableKey k1 = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		ChartInfoTableKey k2 = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		assertTrue(k1.equals(k2));
		assertEquals(k1.hashCode(), k2.hashCode());
	}

	@Test
	public void notEqualsDifferentCityRegion() {
		ChartInfoTableKey k1 = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		ChartInfoTableKey k2 = new ChartInfoTableKey("SEATTLE", "CURRENT", "TAC");
		assertFalse(k1.equals(k2));
	}

	@Test
	public void notEqualsDifferentPeriodCode() {
		ChartInfoTableKey k1 = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		ChartInfoTableKey k2 = new ChartInfoTableKey("DENVER", "NEXT", "TAC");
		assertFalse(k1.equals(k2));
	}

	@Test
	public void notEqualsDifferentChartType() {
		ChartInfoTableKey k1 = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		ChartInfoTableKey k2 = new ChartInfoTableKey("DENVER", "CURRENT", "SECTIONAL");
		assertFalse(k1.equals(k2));
	}

	@Test
	public void notEqualsNull() {
		ChartInfoTableKey key = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		assertFalse(key.equals(null));
	}

	@Test
	public void notEqualsDifferentClass() {
		ChartInfoTableKey key = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		assertFalse(key.equals("not a key"));
	}

	@Test
	public void equalsWithNullFields() {
		ChartInfoTableKey k1 = new ChartInfoTableKey();
		ChartInfoTableKey k2 = new ChartInfoTableKey();
		assertTrue(k1.equals(k2));
		assertEquals(k1.hashCode(), k2.hashCode());
	}

	@Test
	public void equalsNullCityRegionVsNonNull() {
		ChartInfoTableKey k1 = new ChartInfoTableKey();
		ChartInfoTableKey k2 = new ChartInfoTableKey("DENVER", null, null);
		assertFalse(k1.equals(k2));
	}

	@Test
	public void equalsNullPeriodCodeVsNonNull() {
		ChartInfoTableKey k1 = new ChartInfoTableKey("DENVER", null, null);
		ChartInfoTableKey k2 = new ChartInfoTableKey("DENVER", "CURRENT", null);
		assertFalse(k1.equals(k2));
	}

	@Test
	public void equalsNullChartTypeVsNonNull() {
		ChartInfoTableKey k1 = new ChartInfoTableKey("DENVER", "CURRENT", null);
		ChartInfoTableKey k2 = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		assertFalse(k1.equals(k2));
	}

	@Test
	public void hashCodeConsistency() {
		ChartInfoTableKey key = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		int hash1 = key.hashCode();
		int hash2 = key.hashCode();
		assertEquals(hash1, hash2);
	}

	@Test
	public void hashCodeDiffersForDifferentKeys() {
		ChartInfoTableKey k1 = new ChartInfoTableKey("DENVER", "CURRENT", "TAC");
		ChartInfoTableKey k2 = new ChartInfoTableKey("SEATTLE", "NEXT", "SECTIONAL");
		assertNotEquals(k1.hashCode(), k2.hashCode());
	}
}
