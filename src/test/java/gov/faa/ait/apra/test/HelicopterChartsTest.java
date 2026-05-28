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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
public class HelicopterChartsTest {
	private static final Logger logger = LoggerFactory
			.getLogger(HelicopterChartsTest.class);
	private HelicopterCharts helicopter;

	@BeforeEach
	public void initialize() {
		helicopter = new HelicopterCharts();
	}

	static Stream<Date> cycleNumbers() {
		Date[] params = new Date[10];
		GregorianCalendar cal = new GregorianCalendar();
		cal.clear();
		cal.set(2015, 12, 11);
		params[0] = cal.getTime();
		for (int i = 1; i < 6; i++) {
			cal.add(Calendar.DATE, 56);
			params[i] = cal.getTime();
		}
		cal.set(2016, 0, 7);
		params[6] = cal.getTime();
		cal.set(2016, 1, 4);
		params[7] = cal.getTime();
		cal.set(2016, 2, 3);
		params[8] = cal.getTime();
		cal.set(2016, 2, 31);
		params[9] = cal.getTime();
		return Stream.of(params);
	}

	@ParameterizedTest
	@MethodSource("cycleNumbers")
	public void testDownloadOperations(Date date) {
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
			assertEquals(404, code);
		}

		ps = (ProductSet) helicopter.getHelicopterRelease("next", "PDF", "Houston Heli").getEntity();
		code = ps.getStatus().getCode().intValue();

		if (code == 200) {
			logger.info("Status code is " + code);
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
			assertEquals(404, code);
		}
	}

	@ParameterizedTest
	@MethodSource("cycleNumbers")
	public void testEditionOperations(Date date) {
		ProductSet ps = (ProductSet) helicopter.getHelicopterEdition("current", "Houston Heli").getEntity();
		int code = ps.getStatus().getCode().intValue();

		if (code == 200) {
			assertEquals(200, code);
		} else {
			assertEquals(404, code);
		}

		ps = (ProductSet) helicopter.getHelicopterEdition("Next", "Houston Heli").getEntity();
		code = ps.getStatus().getCode().intValue();

		if (code == 200) {
			assertEquals(200, code);
		} else {
			assertEquals(404, code);
		}
	}
}
