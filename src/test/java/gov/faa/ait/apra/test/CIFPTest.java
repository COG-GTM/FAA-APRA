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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.api.BaseService;
import gov.faa.ait.apra.api.CIFP;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleData;

public class CIFPTest {
	private final static Logger logger = LoggerFactory.getLogger(CIFPTest.class);
	private ChartCycleClient client;
	private CIFP cifp;
	private Date releaseDate;

	@BeforeEach
	public void initialize() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.clear();
		cal.set(2015, 5, 1);
		this.releaseDate = cal.getTime();
		client = new ChartCycleClient();
		cifp = new CIFP();
	}

	static Stream<Date> cycleNumbers() {
		Date[] params = new Date[10];
		GregorianCalendar cal = new GregorianCalendar();
		cal.clear();
		cal.set(2015, 0, 1);
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
		ChartCycleData cycle = client.getChartCycle(releaseDate, true);
		if (logger.isDebugEnabled()) {
			logger.debug("Reloaded cycle " + cycle.getName());
		}

		cifp.setFormat(BaseService.ZIP);
		cifp.setEdition(BaseService.CURRENT);

		ProductSet current = cifp.getRelease(client.getCurrent28DayCycle());

		if (current.getEdition().size() > 0)
			logger.info(current.getEdition().get(0).getProduct().getUrl());

		ProductSet next = cifp.getRelease(client.getNext28DayCycle());
		if (next.getEdition().size() > 0)
			logger.info(next.getEdition().get(0).getProduct().getUrl());
	}

	@ParameterizedTest
	@MethodSource("cycleNumbers")
	public void testEditionOperations(Date date) {
		ChartCycleData cycle = client.getChartCycle(releaseDate, true);
		if (logger.isDebugEnabled()) {
			logger.debug("Reloaded cycle " + cycle.getName());
		}

		cifp.setFormat(BaseService.ZIP);
		cifp.setEdition(BaseService.CURRENT);
		ProductSet current = cifp.getEdition(client.getCurrent28DayCycle());
		assertEquals(Integer.valueOf(current.getStatus().getCode()), Integer.valueOf(200));

		ProductSet next = cifp.getEdition(client.getNext28DayCycle());
		assertEquals(Integer.valueOf(next.getStatus().getCode()), Integer.valueOf(200));
	}
}
