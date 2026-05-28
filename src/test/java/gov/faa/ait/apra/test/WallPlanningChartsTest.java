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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.api.WallPlanningCharts;
import gov.faa.ait.apra.jaxb.ProductSet;

/**
 * WallPlanningChartsTest
 * @author FAA
 *
 */
public class WallPlanningChartsTest {
	private static final Logger logger = LoggerFactory
			.getLogger(WallPlanningChartsTest.class);
	private WallPlanningCharts wallPlan;

	@BeforeEach
	public void initialize() {
		wallPlan = new WallPlanningCharts();
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

	@ParameterizedTest
	@MethodSource("cycleNumbers")
	public void testEditionOperations(Date date) {
		ProductSet current = (ProductSet) wallPlan.getProductEdition("current", "pdf").getEntity();
		assertEquals(Integer.valueOf(current.getStatus().getCode()),
			Integer.valueOf(200));

		ProductSet next = (ProductSet) wallPlan.getProductEdition("Next", "pdf").getEntity();

		switch (next.getStatus().getCode()) {
			case 200:
				assertEquals(Integer.valueOf(200), Integer.valueOf(next.getStatus().getCode()));
				break;
			case 404:
				assertEquals(Integer.valueOf(404), Integer.valueOf(next.getStatus().getCode()));
				break;
			default:
				fail();
		}
	}
}
