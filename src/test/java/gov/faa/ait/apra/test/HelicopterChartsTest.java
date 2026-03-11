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
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.api.HelicopterCharts;
import gov.faa.ait.apra.jaxb.ProductSet;

/**
 * HelicopterChartsTest
 * 
 * @author Gangadhar CTR Gouri
 *
 */

@RunWith(Parameterized.class)
public class HelicopterChartsTest {
	private Date releaseDate = null;

	private static final Logger logger = LoggerFactory
			.getLogger(HelicopterChartsTest.class);
	private HelicopterCharts helicopter;

	public HelicopterChartsTest(Date date) {
		this.releaseDate = Date.from(LocalDate.of(2015, 6, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * initialize
	 */
	@Before
	public void initialize() {
		helicopter = new HelicopterCharts();
	}

	/**
	 * cycleNumbers
	 * 
	 * @return
	 */
	@Parameterized.Parameters
	public static Collection<Date> cycleNumbers() {
		LocalDate start = LocalDate.of(2016, 1, 11);
		Date[] params = new Date[10];

		params[0] = toDate(start);

		for (int i = 1; i < 6; i++) {
			start = start.plusDays(56);
			params[i] = toDate(start);
		}

		params[6] = toDate(LocalDate.of(2016, 1, 7));
		params[7] = toDate(LocalDate.of(2016, 2, 4));
		params[8] = toDate(LocalDate.of(2016, 3, 3));
		params[9] = toDate(LocalDate.of(2016, 3, 31));

		return Arrays.asList(params);
	}

	private static Date toDate(LocalDate ld) {
		return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
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
				logger.info("Helicopter Product Relese Test for 'current' return url of {}",
						ps.getEdition().get(0).getProduct().getUrl());
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
			logger.info("Status code is {}", code);
			if ((ps.getEdition().get(0).getEditionDate() != null)
					&& (!ps.getEdition().get(0).getEditionDate().isEmpty())) {
				logger.info("Helicopter Product Relese Test for 'Next' return url of {}",
						ps.getEdition().get(0).getProduct().getUrl());
				
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
