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
import java.util.Date;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.cycle.ChartCycleData;
import gov.faa.ait.apra.util.ChartInfoTable;
import gov.faa.ait.apra.util.ChartInfoTableKey;
import gov.faa.ait.apra.util.TableChartClient;

public class SectionalChartClientTest {
	
	public static Stream<Arguments> cycleNumbers() {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		
		try { 
			return Stream.of(
				Arguments.of(formatter.parse("06/15/2016"), "ALBUQUERQUE", "CURRENT", "SECTIONAL", Integer.valueOf(97)),
				Arguments.of(formatter.parse("06/15/2016"), "ALBUQUERQUE", "NEXT", "SECTIONAL", Integer.valueOf(98))
			);
		}
		catch (ParseException e) {
			return Stream.of(
				Arguments.of(new Date(System.currentTimeMillis()), "ALBUQUERQUE", "CURRENT", "SECTIONAL", Integer.valueOf(1))
			);
		}
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
