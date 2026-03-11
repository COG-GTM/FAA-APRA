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
package gov.faa.ait.apra.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.cycle.ChartCycleClient;

/**
 * This class is used to expand calculation of cycle dates beyond the current and next cycles.
 * The utility uses a calendar to calculate the next after next and previous dates for both
 * the 28 day and 56 day airspace cycle
 * @author FAA
 *
 */
public class CycleDateUtil {
	private static final Logger logger = LoggerFactory.getLogger(CycleDateUtil.class);
	private ChartCycleClient cycleClient;
	
	/**
	 * Default constructor initializes the cycle from the denodo data source
	 */
	public CycleDateUtil () {
		cycleClient = new ChartCycleClient();
	}
	
	/**
	 * Get the current cycle date
	 * @return the current cycle date
	 */
	public Date getCurrentCycle () {
		logger.info("Cycle client in CycleDateUtil is instanceof {}", cycleClient.getClass().getName());
		return cycleClient.getCurrentCycle().getChart_effective_date();
	}
	
	/**
	 * Get the next cycle date
	 * @return the next cycle date
	 */
	public Date getNextCycle () {
		return cycleClient.getNextCycle().getChart_effective_date();
	}	
	
	/**
	 * Get the current 56 day cycle date
	 * @return the current 56 day cycle date
	 */
	public Date getCurrent56Day () {
		return cycleClient.getCurrent56DayCycle().getChart_effective_date();
	}
	
	/**
	 * Get the next 56 day cycle date
	 * @return the next 56 day cycle date
	 */
	public Date getNext56Day () {
		return cycleClient.getNext56DayCycle().getChart_effective_date();
	}

	/**
	 * Get the previous 56 day cycle date. This provides the cycle date that is 1 cycle
	 * prior to the current cycle date.
	 * @return the next after next 56 day cycle date
	 */
	public Date getPrevious56Day () {
		return get56DayCycleDate(-1);
	}
	
	/**
	 * Get the 56 day cycle date by incrementing or decrementing the number of cycles from
	 * the current 56 day cycle date. To get the previous cycle date, the increment = -1. 
	 * To get the "next after next" cycle date, advance 2 cycles and increment = 2. An 
	 * increment value of 0 returns the current 56 day cycle date
	 * @param increment
	 * @return
	 */
	public Date get56DayCycleDate (int increment) {
		LocalDate cycleDate = getCurrent56Day().toInstant()
			.atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate result = cycleDate.plusDays(56L * increment);
		return Date.from(result.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	/**
	 * Get the current 28 day cycle date
	 * @return the current 28 day cycle date
	 */
	public Date getCurrent28Day () {
		return cycleClient.getCurrent28DayCycle().getChart_effective_date();
	}
	
	/**
	 * Get the next 28 day cycle date
	 * @return the next 28 date cycle date
	 */
	public Date getNext28Day () {
		return cycleClient.getNext28DayCycle().getChart_effective_date();		
	}
	
	/**
	 * Get the previous 28 day cycle date. This provides the cycle date that is 1 cycle
	 * prior to the current cycle date.
	 * @return the next after next 28 day cycle date
	 */
	public Date getPrevious28Day () {
		return get28DayCycleDate(-1);
	}
	
	/**
	 * Get the 28 day cycle date by incrementing or decrementing the number of cycles from
	 * the current 28 day cycle date. To get the previous cycle date, the increment = -1. 
	 * To get the "next after next" cycle date, advance 2 cycles and increment = 2. An 
	 * increment value of 0 returns the current 28 day cycle date
	 * @param increment
	 * @return
	 */
	public Date get28DayCycleDate (int increment) {
		LocalDate cycleDate = getCurrent28Day().toInstant()
			.atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate result = cycleDate.plusDays(28L * increment);
		return Date.from(result.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	/**
	 * Get the cycle number for the digital enroute charts. This is a sequential number that increments
	 * by one (1) every 56 days. This starts with an epoch date and cycle of 09/15/2016 and cycle number of 32
	 * @return the current DEC cycle number
	 */
	public static int getCurrentDECCycleNumber () {
		Instant now = Instant.now();
		Instant epoch = LocalDate.of(2011, 10, 20).atTime(0, 1)
			.atZone(ZoneId.systemDefault()).toInstant();
		long days = ChronoUnit.DAYS.between(epoch, now);
		return (int) Math.floor(days / 56d) + 1;
	}
	
	public static int getNextDECCycleNumber () {
		return CycleDateUtil.getCurrentDECCycleNumber() + 1;
	}	
	
	public static int getDECCycleNumber (Calendar startDate) {
		GregorianCalendar epoch = new GregorianCalendar(TimeZone.getDefault());

		// Set the epoch to October 20, 2011 00:01:00
		epoch.set(2011, 9, 20, 0, 1, 0);
		
		logger.debug("Using startDate of {}", startDate);
		logger.debug("Using epoch Date of {}", epoch);
		
		long diff = startDate.getTimeInMillis() - epoch.getTimeInMillis();
		long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		
		logger.debug("Number of days since epoch is {}. Returning cycle number {}", days, Math.floor(days/56d));
		
		return (int) Math.floor(days / 56d)+1;
	}
}
