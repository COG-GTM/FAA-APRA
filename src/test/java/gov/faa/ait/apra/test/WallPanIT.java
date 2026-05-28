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

import gov.faa.ait.apra.api.WallPlanningCharts;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.cycle.WallPlanningChartCycleClient;
import gov.faa.ait.apra.jaxb.ProductSet;

@ExtendWith(MockitoExtension.class)
public class WallPanIT {
	private static final Logger logger = LoggerFactory.getLogger(WallPanIT.class);

	private static ChartCycleElementsJson buildCycleElement() {
		ChartCycleElementsJson e = new ChartCycleElementsJson();
		e.setChart_cycle_period_code("CURRENT");
		e.setChart_cycle_type_code("28 DAY");
		e.setChart_cycle_number("1");
		e.setChart_effective_date(new Date());
		return e;
	}

	@Test
	public void testProductReleseDefault() {
		try (MockedConstruction<WallPlanningChartCycleClient> mocked = Mockito.mockConstruction(
				WallPlanningChartCycleClient.class,
				(mock, context) -> {
					Mockito.when(mock.getCurrentCycle()).thenReturn(buildCycleElement());
					Mockito.when(mock.getNextCycle()).thenReturn(buildCycleElement());
				})) {
			WallPlanningCharts wallPlan = new WallPlanningCharts();
			ProductSet ps = (ProductSet) wallPlan.getProductRelease(null, null).getEntity();
			assertNotNull(ps);
			logger.info("WallPlan Product Relese test for 'Default parameters' return code " + ps.getStatus().getCode());
		}
	}

	@Test
	public void testProductReleseCurrent() {
		try (MockedConstruction<WallPlanningChartCycleClient> mocked = Mockito.mockConstruction(
				WallPlanningChartCycleClient.class,
				(mock, context) -> {
					Mockito.when(mock.getCurrentCycle()).thenReturn(buildCycleElement());
				})) {
			WallPlanningCharts wallPlan = new WallPlanningCharts();
			ProductSet ps = (ProductSet) wallPlan.getProductRelease("current", null).getEntity();
			assertNotNull(ps);
			logger.info("WallPlan Product Relese Test for 'current' return code " + ps.getStatus().getCode());
		}
	}

	@Test
	public void testProductEditionDefault() {
		try (MockedConstruction<WallPlanningChartCycleClient> mocked = Mockito.mockConstruction(
				WallPlanningChartCycleClient.class,
				(mock, context) -> {
					Mockito.when(mock.getCurrentCycle()).thenReturn(buildCycleElement());
					Mockito.when(mock.getNextCycle()).thenReturn(buildCycleElement());
				})) {
			WallPlanningCharts wallPlan = new WallPlanningCharts();
			ProductSet ps = (ProductSet) wallPlan.getProductEdition(null, null).getEntity();
			assertNotNull(ps);
			int code = ps.getStatus().getCode().intValue();
			logger.info("WallPlan Product Edition Test for 'Default parameters' return code " + code);
			assertEquals(200, code);
		}
	}

	@Test
	public void testProductEditionCurrent() {
		try (MockedConstruction<WallPlanningChartCycleClient> mocked = Mockito.mockConstruction(
				WallPlanningChartCycleClient.class,
				(mock, context) -> {
					Mockito.when(mock.getCurrentCycle()).thenReturn(buildCycleElement());
				})) {
			WallPlanningCharts wallPlan = new WallPlanningCharts();
			ProductSet ps = (ProductSet) wallPlan.getProductEdition("current", null).getEntity();
			assertNotNull(ps);
			int code = ps.getStatus().getCode().intValue();
			logger.info("WallPlan Product Edition Test for 'current' return code " + code);
			assertEquals(200, code);
		}
	}

	@Test
	public void testProductEditionTypo() {
		WallPlanningCharts wallPlan = new WallPlanningCharts();
		ProductSet ps = (ProductSet) wallPlan.getProductEdition("Typo", null).getEntity();
		assertNotNull(ps);
		int code = ps.getStatus().getCode().intValue();
		logger.info("WallPlan Product Edition Test for 'Typo' return code " + code);
		assertEquals(400, code);
	}

	@Test
	public void testProductReleseTypo() {
		WallPlanningCharts wallPlan = new WallPlanningCharts();
		ProductSet ps = (ProductSet) wallPlan.getProductRelease("Typo", null).getEntity();
		assertNotNull(ps);
		int code = ps.getStatus().getCode().intValue();
		logger.info("WallPlan Product Relese Test for 'Typo' return code " + code);
		assertEquals(400, code);
	}
}
