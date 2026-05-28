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
import static org.mockito.Mockito.when;

import java.util.Date;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.api.IFREnrouteCharts;
import gov.faa.ait.apra.bootstrap.ErrorCodes;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.jaxb.ProductSet;

@ExtendWith(MockitoExtension.class)
public class IFREnrouteIT {
	private static final Logger logger = LoggerFactory.getLogger(IFREnrouteIT.class);
	private static final String CURRENT = "CURRENT";
	private static final String TIFF = "TIFF";
	private static final String CARIBBEAN = "CARIBBEAN";
	private static final String LOW = "LOW";

	@Mock
	private ChartCycleClient mockClient;

	private IFREnrouteCharts enroute;

	private String editions[] = {"current"};
	private String formats[] = {"pdf", "tiff"};
	private String geos[] = {"us", "alaska"};
	private String alts[] = {"low", "high"};

	private static ChartCycleElementsJson buildCycleElement(String periodCode, String typeCode) {
		ChartCycleElementsJson e = new ChartCycleElementsJson();
		e.setChart_cycle_period_code(periodCode);
		e.setChart_cycle_type_code(typeCode);
		e.setChart_cycle_number("1");
		e.setChart_effective_date(new Date());
		return e;
	}

	@BeforeEach
	public void initialize() {
		when(mockClient.getCurrent56DayCycle()).thenReturn(buildCycleElement("CURRENT", "56 DAY"));
		when(mockClient.getNext56DayCycle()).thenReturn(buildCycleElement("NEXT", "56 DAY"));
		enroute = new IFREnrouteCharts(mockClient);
	}

	@Test
	public void testProductRelese() {
		for (String edition : editions) {
			for (String format : formats) {
				for (String geo : geos) {
					for (String alt : alts) {
						logger.info("Edition: current format: " + format + " geo: " + geo + " alt: " + alt);
						ProductSet ps = (ProductSet) enroute.getIFREnrouteRelease(edition, format, geo, alt).getEntity();
						assertNotNull(ps);
						int code = ps.getStatus().getCode().intValue();
						logger.info("IFREnroute returned code " + code);
					}
				}
			}
		}
	}

	@Test
	public void testBadSet() {
		Response resp = enroute.getIFREnrouteRelease(CURRENT, TIFF, CARIBBEAN, LOW);
		assertEquals(404, resp.getStatus());
		ProductSet ps = (ProductSet) resp.getEntity();
		assertEquals(Integer.valueOf(404), ps.getStatus().getCode());
		assertEquals(ErrorCodes.ERROR_404, ps.getStatus().getMessage());
		assertEquals(0, ps.getEdition().size());
	}

	@Test
	public void testProductEdition() {
		for (String edition : editions) {
			ProductSet ps = (ProductSet) enroute.getIFREnrouteEdition(edition).getEntity();
			assertNotNull(ps);
			int code = ps.getStatus().getCode().intValue();
			logger.info("IFREnroute edition returned code " + code);
		}
	}
}
