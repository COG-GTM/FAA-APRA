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

import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

import gov.faa.ait.apra.util.CycleDateUtil;

public class DECCycleNumberTest {

	@Test
	public void epochDateReturnsCycleOne() {
		GregorianCalendar epoch = new GregorianCalendar(TimeZone.getDefault());
		epoch.set(2011, 9, 20, 0, 2, 0);
		int cycle = CycleDateUtil.getDECCycleNumber(epoch);
		assertEquals(1, cycle);
	}

	@Test
	public void oneCycleAfterEpoch() {
		GregorianCalendar date = new GregorianCalendar(TimeZone.getDefault());
		date.set(2011, 11, 15, 4, 0, 0);
		int cycle = CycleDateUtil.getDECCycleNumber(date);
		assertEquals(2, cycle);
	}

	@Test
	public void year2020Cycle() {
		GregorianCalendar date = new GregorianCalendar(TimeZone.getDefault());
		date.set(2020, 0, 1, 4, 0, 0);
		int cycle = CycleDateUtil.getDECCycleNumber(date);
		assertTrue(cycle > 50);
	}

	@Test
	public void cycleNumberIncreasesOverTime() {
		GregorianCalendar date1 = new GregorianCalendar(TimeZone.getDefault());
		date1.set(2016, 8, 20, 4, 0, 0);

		GregorianCalendar date2 = new GregorianCalendar(TimeZone.getDefault());
		date2.set(2018, 2, 30, 4, 0, 0);

		int cycle1 = CycleDateUtil.getDECCycleNumber(date1);
		int cycle2 = CycleDateUtil.getDECCycleNumber(date2);
		assertTrue(cycle2 > cycle1);
	}

	@Test
	public void getNextDECCycleIsCurrentPlusOne() {
		int current = CycleDateUtil.getCurrentDECCycleNumber();
		int next = CycleDateUtil.getNextDECCycleNumber();
		assertEquals(current + 1, next);
	}

	@Test
	public void getCurrentDECCycleNumberIsPositive() {
		int cycle = CycleDateUtil.getCurrentDECCycleNumber();
		assertTrue(cycle > 0);
	}

	@Test
	public void consecutiveCyclesAre56DaysApart() {
		GregorianCalendar date1 = new GregorianCalendar(TimeZone.getDefault());
		date1.set(2017, 0, 1, 4, 0, 0);
		int cycle1 = CycleDateUtil.getDECCycleNumber(date1);

		GregorianCalendar date2 = new GregorianCalendar(TimeZone.getDefault());
		date2.set(2017, 0, 1, 4, 0, 0);
		date2.add(java.util.Calendar.DATE, 56);
		int cycle2 = CycleDateUtil.getDECCycleNumber(date2);

		assertEquals(cycle1 + 1, cycle2);
	}
}
