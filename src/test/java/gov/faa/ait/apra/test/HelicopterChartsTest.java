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

import gov.faa.ait.apra.api.HelicopterCharts;
import gov.faa.ait.apra.jaxb.ProductSet;

public class HelicopterChartsTest {
	private static final Logger logger = LoggerFactory
			.getLogger(HelicopterChartsTest.class);
	private HelicopterCharts helicopter;

	@BeforeEach
	public void initialize() {
		helicopter = new HelicopterCharts();
	}

	/**
	 * testDownloadOperations
	 */
	@Test
	public void testDownloadOperations() {

		ProductSet ps = (ProductSet) helicopter.getHelicopterRelease("current", "PDF", "Houston Heli").getEntity();
		if (ps.getEdition().size() > 0)
			logger.info(ps.getEdition().get(0).getProduct().getUrl());
		int code = ps.getStatus().getCode().intValue();
		if (code == 200) {
			if ((ps.getEdition().get(0).getEditionDate() != null)
					&& (!ps.getEdition().get(0).getEditionDate().isEmpty())) {
				logger.info("Helicopter Product Relese Test for 'current' return url of "
						+ ps.getEdition().get(0).getProduct().getUrl());
				if (ps.getEdition().get(0).getProduct().getUrl() != null) {

					assertTrue(VerifyValues.verifyURL(ps.getEdition().get(0)
							.getProduct().getUrl()));
				}
			}
		} else {
			assertEquals(code, 404);
		}

		ps = (ProductSet) helicopter.getHelicopterRelease("next", "PDF", "Houston Heli").getEntity();
		code = ps.getStatus().getCode().intValue();

		if (code == 200) {
			logger.info("Status code is "+code);
			if ((ps.getEdition().get(0).getEditionDate() != null)
					&& (!ps.getEdition().get(0).getEditionDate().isEmpty())) {
				logger.info("Helicopter Product Relese Test for 'Next' return url of "
						+ ps.getEdition().get(0).getProduct().getUrl());
				
				if (ps.getEdition().get(0).getProduct().getUrl() != null) {
					assertTrue(VerifyValues.verifyURL(ps.getEdition().get(0)
							.getProduct().getUrl()));
				}
			}
		} else {
			assertEquals(code, 404);
		}

	}

	/**
	 * testEditionOperations
	 */
	@Test
	public void testEditionOperations() {

		ProductSet ps = (ProductSet) helicopter.getHelicopterEdition("current", "Houston Heli").getEntity();
		int code = ps.getStatus().getCode().intValue();

		if (code == 200) {
			assertEquals(code, 200);
		} else {
			assertEquals(code, 404);
		}

		ps = (ProductSet) helicopter.getHelicopterEdition("Next", "Houston Heli").getEntity();
		code = ps.getStatus().getCode().intValue();

		if (code == 200) {
			assertEquals(code, 200);
		} else {
			assertEquals(code, 404);
		}

	}

}
