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
import static org.junit.Assert.assertNull;

import org.junit.Test;

import gov.faa.ait.apra.json.TPPMetadata;

public class TPPMetadataTest {

	@Test
	public void defaultConstructorFieldsAreNull() {
		TPPMetadata meta = new TPPMetadata();
		assertNull(meta.getChart_cycle_period_code());
		assertNull(meta.getChart_edition());
		assertNull(meta.getQuery_date());
		assertEquals(0, meta.getCycle());
		assertNull(meta.getFrom_edate());
		assertNull(meta.getTo_edate());
		assertNull(meta.getId());
		assertNull(meta.getState_fullname());
		assertNull(meta.getVolume());
		assertNull(meta.getCity_name());
		assertNull(meta.getMilitary());
		assertNull(meta.getAirport_identifier());
		assertNull(meta.getAirport_icao_identifier());
		assertNull(meta.getAirport_name());
		assertNull(meta.getChart_name());
		assertNull(meta.getPdf_name());
	}

	@Test
	public void setAndGetChartCyclePeriodCode() {
		TPPMetadata meta = new TPPMetadata();
		meta.setChart_cycle_period_code("Current");
		assertEquals("Current", meta.getChart_cycle_period_code());
	}

	@Test
	public void setAndGetChartEdition() {
		TPPMetadata meta = new TPPMetadata();
		meta.setChart_edition("2401");
		assertEquals("2401", meta.getChart_edition());
	}

	@Test
	public void setAndGetCycle() {
		TPPMetadata meta = new TPPMetadata();
		meta.setCycle(42);
		assertEquals(42, meta.getCycle());
	}

	@Test
	public void setAndGetFromEdate() {
		TPPMetadata meta = new TPPMetadata();
		meta.setFrom_edate("2024-01-01");
		assertEquals("2024-01-01", meta.getFrom_edate());
	}

	@Test
	public void setAndGetToEdate() {
		TPPMetadata meta = new TPPMetadata();
		meta.setTo_edate("2024-02-01");
		assertEquals("2024-02-01", meta.getTo_edate());
	}

	@Test
	public void setAndGetStateFullname() {
		TPPMetadata meta = new TPPMetadata();
		meta.setState_fullname("Nebraska");
		assertEquals("Nebraska", meta.getState_fullname());
	}

	@Test
	public void setAndGetVolume() {
		TPPMetadata meta = new TPPMetadata();
		meta.setVolume("NE-1");
		assertEquals("NE-1", meta.getVolume());
	}

	@Test
	public void setAndGetCityName() {
		TPPMetadata meta = new TPPMetadata();
		meta.setCity_name("Omaha");
		assertEquals("Omaha", meta.getCity_name());
	}

	@Test
	public void setAndGetAirportIdentifier() {
		TPPMetadata meta = new TPPMetadata();
		meta.setAirport_identifier("OMA");
		assertEquals("OMA", meta.getAirport_identifier());
	}

	@Test
	public void setAndGetAirportIcaoIdentifier() {
		TPPMetadata meta = new TPPMetadata();
		meta.setAirport_icao_identifier("KOMA");
		assertEquals("KOMA", meta.getAirport_icao_identifier());
	}

	@Test
	public void setAndGetAirportName() {
		TPPMetadata meta = new TPPMetadata();
		meta.setAirport_name("Eppley Airfield");
		assertEquals("Eppley Airfield", meta.getAirport_name());
	}

	@Test
	public void setAndGetChartName() {
		TPPMetadata meta = new TPPMetadata();
		meta.setChart_name("ILS RWY 14R");
		assertEquals("ILS RWY 14R", meta.getChart_name());
	}

	@Test
	public void setAndGetPdfName() {
		TPPMetadata meta = new TPPMetadata();
		meta.setPdf_name("00078IL14R.PDF");
		assertEquals("00078IL14R.PDF", meta.getPdf_name());
	}

	@Test
	public void setAndGetMilitary() {
		TPPMetadata meta = new TPPMetadata();
		meta.setMilitary("N");
		assertEquals("N", meta.getMilitary());
	}

	@Test
	public void setAndGetId() {
		TPPMetadata meta = new TPPMetadata();
		meta.setId("12345");
		assertEquals("12345", meta.getId());
	}

	@Test
	public void setAndGetQueryDate() {
		TPPMetadata meta = new TPPMetadata();
		meta.setQuery_date("2024-06-15");
		assertEquals("2024-06-15", meta.getQuery_date());
	}
}
