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
import static org.junit.Assert.fail;
import gov.faa.ait.apra.api.VFRCharts;
import gov.faa.ait.apra.jaxb.ProductSet;


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

@RunWith(Parameterized.class)
public class VFRChartsTest {
	@SuppressWarnings("unused")
	private Date releaseDate = null;
	private  static final Logger logger = LoggerFactory.getLogger(VFRChartsTest.class);
	private VFRCharts vfr;

	public VFRChartsTest (Date date) {
		releaseDate = Date.from(LocalDate.of(2015, 6, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
/**
 * initialize
 */
	@Before
	public void initialize() {
		vfr = new VFRCharts();
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
		logger.info("Grand canyon edition current returned HTTP status code {}", current.getStatus().getCode());
		assertEquals(new Integer(200), new Integer(current.getStatus().getCode()));

		ProductSet next = (ProductSet) vfr.getGrandCanyonEdition("Next").getEntity();
		logger.info("Grand canyon edition next returned HTTP status code {}", current.getStatus().getCode());
		
		int code = next.getStatus().getCode().intValue();
		
		switch (code) {
			case 200: assertEquals(Integer.valueOf(200), Integer.valueOf(next.getStatus().getCode()));
				break;
			case 404: assertEquals(Integer.valueOf(404), Integer.valueOf(next.getStatus().getCode()));
				break;
			
			default:
				fail();
		}
		
	}

}
