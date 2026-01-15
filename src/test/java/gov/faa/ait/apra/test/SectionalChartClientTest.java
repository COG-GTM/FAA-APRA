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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.cycle.ChartCycleData;
import gov.faa.ait.apra.util.ChartInfoTable;
import gov.faa.ait.apra.util.ChartInfoTableKey;
import gov.faa.ait.apra.util.TableChartClient;

public class SectionalChartClientTest {

	private TableChartClient client;
	
	@BeforeEach
	public void init() {
		this.client = new TableChartClient();
	}
	
	public static Stream<Arguments> cycleNumbers () {
		//ArrayList <Date> arrayList = new ArrayList <Date>();	
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		Object [] [] params = null;
		
		try { 
			params = new Object [] [] {
				{formatter.parse("06/15/2016"), "ALBUQUERQUE", "CURRENT", "SECTIONAL", Integer.valueOf(97)},
				{formatter.parse("06/15/2016"), "ALBUQUERQUE", "NEXT", "SECTIONAL", Integer.valueOf(98)}
				/*
				{formatter.parse("11/10/2016"), new Integer(6)},
				{formatter.parse("01/01/2017"), new Integer(6)},
				{formatter.parse("12/25/2016"), new Integer(6)},
				{formatter.parse("01/05/2017"), new Integer(1)},
				{formatter.parse("02/03/2017"), new Integer(1)},
				{formatter.parse("06/14/2017"), new Integer(3)},
				{formatter.parse("09/15/2017"), new Integer(5)},
				{formatter.parse("11/09/2017"), new Integer(6)}
				
				*/
			};
		}
		catch (ParseException e) {
			params = new Object [] [] {
				{new Date(System.currentTimeMillis()), Integer.valueOf(1) }
			};
		}

		return Arrays.stream(params).map(Arguments::of);
	}	
	@ParameterizedTest
	@MethodSource("cycleNumbers")
	public void test(Date targetDate, String cityKey, String editionKey, String typeKey, Integer expectedVersion) {
		
		ChartCycleData jsonChart = TableChartClient.callResource(targetDate);
		ChartInfoTable table = new ChartInfoTable(jsonChart);
		ChartInfoTableKey key = new ChartInfoTableKey();
		key.setCityRegion(cityKey);
		key.setPeriodCode(editionKey);
		key.setChartType(typeKey);
		ChartCycleElementsJson element = table.get(key);
		assertEquals(expectedVersion, Integer.valueOf(Integer.parseInt(element.getChart_cycle_number())));
	}

}
