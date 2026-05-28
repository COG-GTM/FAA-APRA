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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.api.HelicopterCharts;
import gov.faa.ait.apra.cycle.ChartCycleData;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.util.ChartInfoTable;
import gov.faa.ait.apra.util.TableChartClient;

@ExtendWith(MockitoExtension.class)
public class HelicopterIT {
	private static final Logger logger = LoggerFactory.getLogger(HelicopterIT.class);
	private HelicopterCharts helicopter;

	private String cities[] = {
		"Boston Heli", "Chicago Heli", "Detroit Heli", "Houston Heli",
		"Los Angeles Heli", "New York Heli", "Baltimore Washington Heli",
		"Dallas Ft. Worth Heli", "U.S Gulf Coast"
	};

	@Mock
	private TableChartClient mockClient;

	@BeforeEach
	public void initialize() {
		helicopter = new HelicopterCharts(mockClient);
	}

	@Test
	public void testProductReleseDefault() {
		try (MockedStatic<TableChartClient> mocked = Mockito.mockStatic(TableChartClient.class)) {
			mocked.when(() -> TableChartClient.getTable(any(TableChartClient.class))).thenReturn(null);
			for (String city : cities) {
				ProductSet ps = (ProductSet) helicopter.getHelicopterRelease("", null, city).getEntity();
				assertNotNull(ps);
				assertNotNull(ps.getStatus());
			}
		}
	}

	@Test
	public void testProductEditionTypo() {
		ProductSet ps = (ProductSet) helicopter.getHelicopterEdition("TYPO", "Boston Heli").getEntity();
		int code = ps.getStatus().getCode().intValue();
		logger.info("Helicopter Product Edition Test for 'incorrect edition' return code " + code);
		assertEquals(400, code);
	}

	@Test
	public void testProductReleaseTypo() {
		ProductSet ps = (ProductSet) helicopter.getHelicopterRelease("TYPO", "PDF", "Boston Heli").getEntity();
		int code = ps.getStatus().getCode().intValue();
		logger.info("Helicopter Product Relese Test for 'incorrect Release' return code " + code);
		assertEquals(400, code);
	}

	@Test
	public void testProductEditionGeoTypo() {
		ProductSet ps = (ProductSet) helicopter.getHelicopterEdition("", "Typo").getEntity();
		int code = ps.getStatus().getCode().intValue();
		logger.info("Helicopter Product Edition Test for 'incorrect city' return code " + code);
		assertEquals(400, code);
	}

	@Test
	public void testProductReleaseGeoTypo() {
		ProductSet ps = (ProductSet) helicopter.getHelicopterRelease("", "PDF", "Typo").getEntity();
		int code = ps.getStatus().getCode().intValue();
		logger.info("Helicopter Product Release Test for 'incorrect city' return code " + code);
		assertEquals(400, code);
	}

	@Test
	public void testProductEditionTypoTypo() {
		ProductSet ps = (ProductSet) helicopter.getHelicopterEdition("Typo", "Typo").getEntity();
		int code = ps.getStatus().getCode().intValue();
		logger.info("Helicopter Product Edition Test for 'incorrect edition and Fmt' return code " + code);
		assertEquals(400, code);
	}

	@Test
	public void testProductReleaseFmtTypoTypo() {
		ProductSet ps = (ProductSet) helicopter.getHelicopterRelease("Typo", "PDF", "Typo").getEntity();
		int code = ps.getStatus().getCode().intValue();
		logger.info("Helicopter Product Release Test for 'incorrect Release and city' return code " + code);
		assertEquals(400, code);
	}
}
