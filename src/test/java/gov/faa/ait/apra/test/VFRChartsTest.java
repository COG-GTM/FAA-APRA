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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.api.VFRCharts;
import gov.faa.ait.apra.jaxb.ProductSet;

public class VFRChartsTest {
	private static final Logger logger = LoggerFactory.getLogger(VFRChartsTest.class);
	private VFRCharts vfr;

	@BeforeEach
	public void initialize() {
		vfr = new VFRCharts();
	}

	/**
	 * testDownloadOperations
	 */
	@Test
	public void testDownloadOperations() {
		
		ProductSet current = (ProductSet) vfr.getGrandCanyonRelease("current").getEntity();
		if (current.getEdition().size() > 0)
			logger.info(current.getEdition().get(0).getProduct().getUrl());
		assertEquals(Integer.valueOf(current.getStatus().getCode()), 
				Integer.valueOf(200));
		ProductSet next = (ProductSet) vfr.getGrandCanyonRelease("next").getEntity();
		if (next.getEdition().size() > 0)
			logger.info(next.getEdition().get(0).getProduct().getUrl());
		assertEquals(Integer.valueOf(next.getStatus().getCode()), Integer.valueOf(404));
	}

	/**
	 * testEditionOperations
	*/
	@Test
	public void testEditionOperations() {
		ProductSet current = (ProductSet) vfr.getGrandCanyonEdition("current").getEntity();
		logger.info("Grand canyon edition current returned HTTP status code "+current.getStatus().getCode());
		assertEquals(new Integer(200), new Integer(current.getStatus().getCode()));

		ProductSet next = (ProductSet) vfr.getGrandCanyonEdition("Next").getEntity();
		logger.info("Grand canyon edition next returned HTTP status code "+current.getStatus().getCode());
		
		int code = next.getStatus().getCode().intValue();
		
		assertTrue(code == 200 || code == 404, 
			"Expected 200 or 404 response code, got: " + code);
		
	}

}
