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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.cycle.ChartCycleData;
import gov.faa.ait.apra.util.ChartInfoTable;
import gov.faa.ait.apra.util.ChartInfoTableKey;
import gov.faa.ait.apra.util.TableChartClient;

@ExtendWith(MockitoExtension.class)
public class SectionalChartClientTest {

	static Stream<Arguments> cycleNumbers() {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		try {
			return Stream.of(
				Arguments.of(formatter.parse("06/15/2016"), "ALBUQUERQUE", "CURRENT", "SECTIONAL", 97),
				Arguments.of(formatter.parse("06/15/2016"), "ALBUQUERQUE", "NEXT", "SECTIONAL", 98)
			);
		} catch (ParseException e) {
			return Stream.of(
				Arguments.of(new Date(), "ALBUQUERQUE", "CURRENT", "SECTIONAL", 1)
			);
		}
	}

	private static ChartCycleElementsJson buildElement(String city, String periodCode, String type, int cycle) {
		ChartCycleElementsJson e = new ChartCycleElementsJson();
		e.setChart_city_name(city);
		e.setChart_cycle_period_code(periodCode);
		e.setChart_cycle_type_code(type);
		e.setChart_cycle_number(String.valueOf(cycle));
		e.setChart_effective_date(new Date());
		return e;
	}

	@ParameterizedTest
	@MethodSource("cycleNumbers")
	public void test(Date targetDate, String cityKey, String editionKey, String typeKey, int expectedVersion) {
		ChartCycleElementsJson currentElement = buildElement(cityKey, "CURRENT", typeKey, 97);
		ChartCycleElementsJson nextElement = buildElement(cityKey, "NEXT", typeKey, 98);
		ChartCycleElementsJson[] elements = new ChartCycleElementsJson[] { currentElement, nextElement };
		ChartCycleData mockData = new ChartCycleData("vfr_chart_cycle", elements);

		try (MockedStatic<TableChartClient> mockedStatic = Mockito.mockStatic(TableChartClient.class)) {
			mockedStatic.when(() -> TableChartClient.callResource(Mockito.any(Date.class)))
				.thenReturn(mockData);

			ChartCycleData jsonChart = TableChartClient.callResource(targetDate);
			assertNotNull(jsonChart);
			ChartInfoTable table = new ChartInfoTable(jsonChart);
			ChartInfoTableKey key = new ChartInfoTableKey();
			key.setCityRegion(cityKey);
			key.setPeriodCode(editionKey);
			key.setChartType(typeKey);
			ChartCycleElementsJson element = table.get(key);
			assertNotNull(element);
			assertEquals(expectedVersion, Integer.parseInt(element.getChart_cycle_number()));
		}
	}
}
