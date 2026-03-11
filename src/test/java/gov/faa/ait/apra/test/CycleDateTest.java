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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.util.CycleDateUtil;

public class CycleDateTest {
	private static final Logger logger = LoggerFactory.getLogger(CycleDateTest.class);

	@Test
	public void get56Day2CyclesAhead () {
		CycleDateUtil cdu = new CycleDateUtil();
		Date d = cdu.get56DayCycleDate(2);
		logger.info("Next 56 day cycle date is {}", cdu.getNext56Day());
		logger.info("Next after next 56 day cycle date is {}", d);
	}
	
	@Test
	public void getBeforeCurrent56 () {
		CycleDateUtil cdu = new CycleDateUtil();
		Date d = cdu.getPrevious56Day();
		logger.info("Current 56 day cycle date is {}", cdu.getCurrent56Day());
		logger.info("Cycle before current 56 day cycle date is {}", d);
	}
	
	@Test
	public void get28Day2CyclesAhead () {
		CycleDateUtil cdu = new CycleDateUtil();
		Date d = cdu.get28DayCycleDate(2);
		logger.info("Next 28 day cycle date is {}", cdu.getNext28Day());
		logger.info("Next after next 28 day cycle date is {}", d);
	}
	
	@Test
	public void getBeforeCurrent28 () {
		CycleDateUtil cdu = new CycleDateUtil();
		Date d = cdu.getPrevious28Day();
		logger.info("Current 28 day cycle date is {}", cdu.getCurrent28Day());
		logger.info("Cycle before current 28 day cycle date is {}", d);
	}
	
	@Test
	public void getDECCycle1 () {
		GregorianCalendar epoch = GregorianCalendar.from(ZonedDateTime.of(2016, 9, 20, 4, 0, 0, 0, ZoneId.systemDefault()));

		int cycle = CycleDateUtil.getDECCycleNumber(epoch);

		logger.info("Calculated cycle number {} for date 09/20/2016", cycle);
		assertEquals(cycle, 33);
	}
	
	@Test
	public void getDECCycle2 () {
		GregorianCalendar epoch = GregorianCalendar.from(ZonedDateTime.of(2016, 12, 25, 4, 0, 0, 0, ZoneId.systemDefault()));

		int cycle = CycleDateUtil.getDECCycleNumber(epoch);

		logger.info("Calculated cycle number {} for date 12/25/2016", cycle);
		
		assertEquals(cycle, 34);
	}
	
	@Test
	public void getDECCycle3 () {
		GregorianCalendar epoch = GregorianCalendar.from(ZonedDateTime.of(2018, 3, 30, 4, 0, 0, 0, ZoneId.systemDefault()));

		int cycle = CycleDateUtil.getDECCycleNumber(epoch);
		logger.info("Calculated cycle number {} for date 03/30/2018", cycle);
		
		assertEquals(cycle, 43);
	}
	
	@Test
	public void getDECCycle4 () {
		GregorianCalendar epoch = GregorianCalendar.from(ZonedDateTime.of(2017, 6, 22, 4, 0, 0, 0, ZoneId.systemDefault()));

		int cycle = CycleDateUtil.getDECCycleNumber(epoch);
		logger.info("Calculated cycle number {} for date 06/22/2017", cycle);
		
		assertEquals(cycle, 38);
	}
}
