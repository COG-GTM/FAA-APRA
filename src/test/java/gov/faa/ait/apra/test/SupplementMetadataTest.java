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

import gov.faa.ait.apra.json.SupplementMetadata;

public class SupplementMetadataTest {

	@Test
	public void defaultConstructorInitializesEmptyStrings() {
		SupplementMetadata meta = new SupplementMetadata();
		assertEquals("", meta.getState());
		assertEquals("", meta.getAptname());
		assertEquals("", meta.getAptcity());
		assertEquals("", meta.getAptid());
		assertEquals("", meta.getPdf());
		assertEquals("", meta.getNavidname());
		assertEquals("", meta.getChartDate());
		assertEquals("", meta.getVolumeAbbreviation());
		assertEquals("", meta.getVolumeName());
	}

	@Test
	public void setAndGetState() {
		SupplementMetadata meta = new SupplementMetadata();
		meta.setState("NE");
		assertEquals("NE", meta.getState());
	}

	@Test
	public void setAndGetAptname() {
		SupplementMetadata meta = new SupplementMetadata();
		meta.setAptname("Eppley Airfield");
		assertEquals("Eppley Airfield", meta.getAptname());
	}

	@Test
	public void setAndGetAptcity() {
		SupplementMetadata meta = new SupplementMetadata();
		meta.setAptcity("Omaha");
		assertEquals("Omaha", meta.getAptcity());
	}

	@Test
	public void setAndGetAptid() {
		SupplementMetadata meta = new SupplementMetadata();
		meta.setAptid("OMA");
		assertEquals("OMA", meta.getAptid());
	}

	@Test
	public void setAndGetPdf() {
		SupplementMetadata meta = new SupplementMetadata();
		meta.setPdf("chart.pdf");
		assertEquals("chart.pdf", meta.getPdf());
	}

	@Test
	public void setAndGetNavidname() {
		SupplementMetadata meta = new SupplementMetadata();
		meta.setNavidname("VOR");
		assertEquals("VOR", meta.getNavidname());
	}

	@Test
	public void setAndGetChartDate() {
		SupplementMetadata meta = new SupplementMetadata();
		meta.setChartDate("2024-01-01");
		assertEquals("2024-01-01", meta.getChartDate());
	}

	@Test
	public void setAndGetVolumeAbbreviation() {
		SupplementMetadata meta = new SupplementMetadata();
		meta.setVolumeAbbreviation("NE");
		assertEquals("NE", meta.getVolumeAbbreviation());
	}

	@Test
	public void setAndGetVolumeName() {
		SupplementMetadata meta = new SupplementMetadata();
		meta.setVolumeName("Northeast");
		assertEquals("Northeast", meta.getVolumeName());
	}
}
