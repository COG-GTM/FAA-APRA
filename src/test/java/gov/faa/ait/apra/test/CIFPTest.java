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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.api.BaseService;
import gov.faa.ait.apra.api.CIFP;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleData;

@RunWith(Parameterized.class)
public class CIFPTest {
	private Date releaseDate = null;
	
	private final static Logger logger = LoggerFactory.getLogger(CIFPTest.class);
	private ChartCycleClient client;
	private CIFP cifp;
	
	public CIFPTest (Date date) {
		this.releaseDate = Date.from(LocalDate.of(2015, 6, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	@Before
	public void initialize() {
		client = new ChartCycleClient();
		cifp = new CIFP();
	}
	
	@Parameterized.Parameters
	public static Collection<Date> cycleNumbers () {
		LocalDate start = LocalDate.of(2015, 1, 1);
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

	@Test
	public void testDownloadOperations() {
		ChartCycleData cycle = client.getChartCycle(releaseDate, true);
		if (logger.isDebugEnabled()) {
			logger.debug("Reloaded cycle {}", cycle.getName());
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
	
	@Test
	public void testEditionOperations() {
		ChartCycleData cycle = client.getChartCycle(releaseDate, true);
		if (logger.isDebugEnabled()) {
			logger.debug("Reloaded cycle {}", cycle.getName());
		}
		
		cifp.setFormat(BaseService.ZIP);
		cifp.setEdition(BaseService.CURRENT);		
		ProductSet current = cifp.getEdition(client.getCurrent28DayCycle());
		assertEquals(new Integer(current.getStatus().getCode()), Integer.valueOf(200));	
		
		ProductSet next = cifp.getEdition(client.getNext28DayCycle());
		assertEquals(new Integer(next.getStatus().getCode()), Integer.valueOf(200));
	}	
	
}
