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

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.api.SupplementCharts;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.jaxb.ProductSet;

@ExtendWith(MockitoExtension.class)
public class SupplementIT {
	private static final Logger logger = LoggerFactory.getLogger(SupplementIT.class);

	private String cities[] = {
		"NORTHWEST", "SOUTHWEST", "NORTH CENTRAL",
		"SOUTH CENTRAL", "EAST CENTRAL", "SOUTHEAST", "NORTHEAST",
		"PACIFIC", "ALASKA"
	};

	private static ChartCycleElementsJson buildCycleElement(String periodCode) {
		ChartCycleElementsJson e = new ChartCycleElementsJson();
		e.setChart_cycle_period_code(periodCode);
		e.setChart_cycle_type_code("56 DAY");
		e.setChart_cycle_number("1");
		e.setChart_effective_date(new Date());
		return e;
	}

	@Test
	public void testProductEditionDefault() {
		try (MockedConstruction<ChartCycleClient> mocked = Mockito.mockConstruction(
				ChartCycleClient.class,
				(mock, context) -> {
					Mockito.when(mock.getCurrent56DayCycle()).thenReturn(buildCycleElement("CURRENT"));
					Mockito.when(mock.getNext56DayCycle()).thenReturn(buildCycleElement("NEXT"));
				})) {
			SupplementCharts supplement = new SupplementCharts();
			for (String city : cities) {
				ProductSet ps = (ProductSet) supplement.getSupplementEdition("", city).getEntity();
				assertNotNull(ps);
				int code = ps.getStatus().getCode().intValue();
				logger.info("Supplement Product Edition Test for '" + city + "' return code " + code);
				assertEquals(200, code);
			}
		}
	}

	@Test
	public void testProductEditionDefaultUS() {
		try (MockedConstruction<ChartCycleClient> mocked = Mockito.mockConstruction(
				ChartCycleClient.class,
				(mock, context) -> {
					Mockito.when(mock.getCurrent56DayCycle()).thenReturn(buildCycleElement("CURRENT"));
					Mockito.when(mock.getNext56DayCycle()).thenReturn(buildCycleElement("NEXT"));
				})) {
			SupplementCharts supplement = new SupplementCharts();
			ProductSet ps = (ProductSet) supplement.getSupplementEdition("", "").getEntity();
			assertNotNull(ps);
			int code = ps.getStatus().getCode().intValue();
			logger.info("Supplement Product Edition Test for 'Default US' return code " + code);
			assertEquals(200, code);
		}
	}

	@Test
	public void testProductEditionVOLTypo() {
		try (MockedConstruction<ChartCycleClient> mocked = Mockito.mockConstruction(
				ChartCycleClient.class,
				(mock, context) -> {
					Mockito.when(mock.getCurrent56DayCycle()).thenReturn(buildCycleElement("CURRENT"));
					Mockito.when(mock.getNext56DayCycle()).thenReturn(buildCycleElement("NEXT"));
				})) {
			SupplementCharts supplement = new SupplementCharts();
			ProductSet ps = (ProductSet) supplement.getSupplementEdition("", "Typo").getEntity();
			assertNotNull(ps);
			int code = ps.getStatus().getCode().intValue();
			logger.info("Supplement Product Edition Test for 'Typo' return code " + code);
			assertEquals(404, code);
		}
	}

	@Test
	public void testProductReleseVOLTypo() {
		try (MockedConstruction<ChartCycleClient> mocked = Mockito.mockConstruction(
				ChartCycleClient.class,
				(mock, context) -> {
					Mockito.when(mock.getCurrent56DayCycle()).thenReturn(buildCycleElement("CURRENT"));
					Mockito.when(mock.getNext56DayCycle()).thenReturn(buildCycleElement("NEXT"));
				})) {
			SupplementCharts supplement = new SupplementCharts();
			ProductSet ps = (ProductSet) supplement.getSupplementRelease("", "Typo").getEntity();
			assertNotNull(ps);
			int code = ps.getStatus().getCode().intValue();
			logger.info("Supplement Product Relese Test for 'Typo' return code " + code);
			assertEquals(404, code);
		}
	}
}
