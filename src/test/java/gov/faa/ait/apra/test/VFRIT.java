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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.api.VFRCharts;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.cycle.VFRChartCycleClient;
import gov.faa.ait.apra.jaxb.ProductSet;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class VFRIT {
	private static final Logger logger = LoggerFactory.getLogger(VFRIT.class);

	private static ChartCycleElementsJson buildCycleElement() {
		ChartCycleElementsJson e = new ChartCycleElementsJson();
		e.setChart_cycle_period_code("CURRENT");
		e.setChart_cycle_type_code("28 DAY");
		e.setChart_cycle_number("1");
		e.setChart_effective_date(new Date());
		e.setChart_city_name("Grand Canyon");
		return e;
	}

	@Test
	public void testProductEditionTypo() {
		VFRCharts vfr = new VFRCharts();
		ProductSet ps = (ProductSet) vfr.getGrandCanyonEdition("TYPO").getEntity();
		int code = ps.getStatus().getCode().intValue();
		logger.info("VFR Product Edition Test for 'incorrect edition' return code " + code);
		assertEquals(400, code);
	}

	@Test
	public void testProductReleaseTypo() {
		VFRCharts vfr = new VFRCharts();
		ProductSet ps = (ProductSet) vfr.getGrandCanyonRelease("TYPO").getEntity();
		int code = ps.getStatus().getCode().intValue();
		logger.info("VFR Product Relese Test for 'incorrect Release' return code " + code);
		assertEquals(400, code);
	}

	@Test
	public void testProductEditionCurrent() {
		try (MockedConstruction<VFRChartCycleClient> mocked = Mockito.mockConstruction(
				VFRChartCycleClient.class,
				(mock, context) -> {
					Mockito.when(mock.getCurrentCycle()).thenReturn(buildCycleElement());
					Mockito.when(mock.getNextCycle()).thenReturn(buildCycleElement());
				})) {
			VFRCharts vfr = new VFRCharts();
			ProductSet ps = (ProductSet) vfr.getGrandCanyonEdition("current").getEntity();
			assertNotNull(ps);
			int code = ps.getStatus().getCode().intValue();
			logger.info("VFR Product Edition Test for 'current' return code " + code);
			assertEquals(200, code);
		}
	}

	@Test
	public void testProductEditionDefault() {
		try (MockedConstruction<VFRChartCycleClient> mocked = Mockito.mockConstruction(
				VFRChartCycleClient.class,
				(mock, context) -> {
					Mockito.when(mock.getCurrentCycle()).thenReturn(buildCycleElement());
				})) {
			VFRCharts vfr = new VFRCharts();
			ProductSet ps = (ProductSet) vfr.getGrandCanyonEdition("").getEntity();
			assertNotNull(ps);
			int code = ps.getStatus().getCode().intValue();
			logger.info("VFR Product Edition Test for 'empty and empty' return code " + code);
			assertEquals(200, code);
		}
	}

	@Test
	public void testProductReleaseDefault() {
		try (MockedConstruction<VFRChartCycleClient> mocked = Mockito.mockConstruction(
				VFRChartCycleClient.class,
				(mock, context) -> {
					Mockito.when(mock.getCurrentCycle()).thenReturn(buildCycleElement());
				})) {
			VFRCharts vfr = new VFRCharts();
			ProductSet ps = (ProductSet) vfr.getGrandCanyonRelease("").getEntity();
			assertNotNull(ps);
			int code = ps.getStatus().getCode().intValue();
			logger.info("VFR Product Release Test for 'empty' return code " + code);
			assertEquals(200, code);
		}
	}

	@Test
	public void testProductEditionNext() {
		try (MockedConstruction<VFRChartCycleClient> mocked = Mockito.mockConstruction(
				VFRChartCycleClient.class,
				(mock, context) -> {
					Mockito.when(mock.getNextCycle()).thenReturn(buildCycleElement());
				})) {
			VFRCharts vfr = new VFRCharts();
			ProductSet ps = (ProductSet) vfr.getGrandCanyonEdition("next").getEntity();
			assertNotNull(ps);
			int code = ps.getStatus().getCode().intValue();
			logger.info("VFR Product Edition Test for 'next' return code " + code);
			switch (code) {
				case 200:
					assertEquals(200, code);
					break;
				case 404:
					assertEquals(404, code);
					break;
				default:
					fail();
			}
		}
	}
}
