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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import gov.faa.ait.apra.api.IFREnrouteCharts;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleData;

/**
 * HelicopterChartsTest
 * @author Gangadhar CTR Gouri
 *
 */
		
@RunWith(Parameterized.class)
public class IFREnrouteChartsTest {
	private Date releaseDate = null;
	private static boolean setupComplete = false;

	private static final Logger logger = LoggerFactory
			.getLogger(HelicopterChartsTest.class);
	private IFREnrouteCharts chats;

	public IFREnrouteChartsTest (Date date) {
		IFREnrouteChartsTest.setup();
		this.releaseDate = Date.from(LocalDate.of(2015, 6, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}	
	
	private static void setup () {
		if (IFREnrouteChartsTest.setupComplete) {
			return;
		}
		ChartCycleClient client = new ChartCycleClient();
		ChartCycleData cycle = client.getChartCycle(new Date (System.currentTimeMillis()), true);
		logger.info("Updated chart cycle in prep for IFR Enroute tests {}", cycle.getName());
		IFREnrouteChartsTest.setupComplete = true;
	}
	
	/**
	 * initialize
	 */
	@Before
	public void initialize() {
		chats = new IFREnrouteCharts();
	}
/**
 * cycleNumbers
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
		
		ProductSet current = (ProductSet) chats.getIFREnrouteRelease("current", "tiff", "US", "low").getEntity();
		if (current.getEdition().size() > 0)
			logger.info(current.getEdition().get(0).getProduct().getUrl());
		assertEquals(Integer.valueOf(current.getStatus().getCode()), Integer.valueOf(200));
	}

	/**
	 * testEditionOperations
	 */
	@Test
	public void testEditionOperations() {
		
		ProductSet current = (ProductSet) chats.getIFREnrouteEdition("current").getEntity();
		assertEquals(Integer.valueOf(current.getStatus().getCode()), Integer.valueOf(
				200));

		ProductSet next = (ProductSet) chats.getIFREnrouteEdition("Next").getEntity();
		assertEquals(Integer.valueOf(next.getStatus().getCode()), Integer.valueOf(200));
		
		
	}
}
