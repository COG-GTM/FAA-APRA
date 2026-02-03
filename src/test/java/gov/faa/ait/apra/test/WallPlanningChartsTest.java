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

import gov.faa.ait.apra.api.WallPlanningCharts;
import gov.faa.ait.apra.jaxb.ProductSet;

public class WallPlanningChartsTest {
	private static final Logger logger = LoggerFactory
			.getLogger(WallPlanningChartsTest.class);
	private WallPlanningCharts wallPlan;

	@BeforeEach
	public void initialize() {
		wallPlan = new WallPlanningCharts();
	}

	/**
	 * testDownloadOperations
	 */
	@Test
	public void testDownloadOperations() {
		
		ProductSet current = (ProductSet) wallPlan.getProductRelease("current", "pdf").getEntity();
		if (current.getEdition().size() > 0)
			logger.info(current.getEdition().get(0).getProduct().getUrl());
		assertEquals(Integer.valueOf(current.getStatus().getCode()), 
			Integer.valueOf(200));
		ProductSet next = (ProductSet) wallPlan.getProductRelease("next", "pdf").getEntity();
		if (next.getEdition().size() > 0)
			logger.info(next.getEdition().get(0).getProduct().getUrl());
		assertEquals(Integer.valueOf(next.getStatus().getCode()), Integer.valueOf(404));
	}

	/**
	 * testEditionOperations
	 */
	@Test
	public void testEditionOperations() {
		ProductSet current = (ProductSet) wallPlan.getProductEdition("current", "pdf").getEntity();
		assertEquals(Integer.valueOf(current.getStatus().getCode()), 
			Integer.valueOf(200));

		ProductSet next = (ProductSet) wallPlan.getProductEdition("Next", "pdf").getEntity();
			
		int code = next.getStatus().getCode();
		assertTrue(code == 200 || code == 404, 
			"Expected 200 or 404 response code, got: " + code);
		
	}
}
