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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.api.IFREnrouteCharts;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;

@ExtendWith(MockitoExtension.class)
@DisplayName("IFR Enroute Product Set Tests")
public class IFREnrouteProductSetTest {
	
	private static final Logger logger = LoggerFactory.getLogger(IFREnrouteProductSetTest.class);
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy");
	
	@Mock
	private ChartCycleClient mockCycleClient;
	
	private ChartCycleElementsJson mockCycleElement;
	
	@BeforeEach
	void setUp() {
		mockCycleElement = createMockCycleElement();
		lenient().when(mockCycleClient.getCurrent56DayCycle()).thenReturn(mockCycleElement);
		lenient().when(mockCycleClient.getNext56DayCycle()).thenReturn(mockCycleElement);
		lenient().when(mockCycleClient.getChartCycle(any(Date.class), anyBoolean())).thenReturn(createMockChartCycleData());
	}
	
	private ChartCycleElementsJson createMockCycleElement() {
		ChartCycleElementsJson element = new ChartCycleElementsJson();
		GregorianCalendar cal = new GregorianCalendar();
		element.setChart_effective_date(cal.getTime());
		element.setChart_cycle_number("1");
		element.setChart_cycle_period_code("CURRENT");
		return element;
	}
	
	private gov.faa.ait.apra.cycle.ChartCycleData createMockChartCycleData() {
		gov.faa.ait.apra.cycle.ChartCycleData data = mock(gov.faa.ait.apra.cycle.ChartCycleData.class);
		ChartCycleElementsJson[] elements = new ChartCycleElementsJson[] { mockCycleElement };
		when(data.getElements()).thenReturn(elements);
		return data;
	}
	
	static Stream<Arguments> provideTestParameters() {
		return Stream.of(
			Arguments.of("CURRENT", "US", "LOW", "TIFF", 36),
			Arguments.of("CURRENT", "US", "LOW", "PDF", 18),
			Arguments.of("CURRENT", "US", "HIGH", "TIFF", 12),
			Arguments.of("CURRENT", "US", "HIGH", "PDF", 6),
			Arguments.of("CURRENT", "ALASKA", "LOW", "TIFF", 4),
			Arguments.of("CURRENT", "ALASKA", "LOW", "PDF", 2),
			Arguments.of("CURRENT", "ALASKA", "HIGH", "TIFF", 2),
			Arguments.of("CURRENT", "ALASKA", "HIGH", "PDF", 1),
			Arguments.of("CURRENT", "PACIFIC", "HIGH", "TIFF", 2),
			Arguments.of("CURRENT", "PACIFIC", "HIGH", "PDF", 1),
			Arguments.of("CURRENT", "CARIBBEAN", "LOW", "PDF", 3),
			Arguments.of("CURRENT", "CARIBBEAN", "HIGH", "PDF", 1),
			Arguments.of("CURRENT", "US", "AREA", "TIFF", 2),
			Arguments.of("CURRENT", "US", "AREA", "PDF", 1),
			Arguments.of("CURRENT", "CARIBBEAN", "AREA", "PDF", 2)
		);
	}
	
	@ParameterizedTest(name = "Test IFR Enroute: edition={0}, geoname={1}, seriesType={2}, format={3}")
	@MethodSource("provideTestParameters")
	@DisplayName("IFR Enroute Chart Product Test")
	void testIFREnrouteProduct(String edition, String geoname, String seriesType, String format, int expectedCount) {
		logger.info("Testing geoname={}, edition={}, format={}, seriesType={}", geoname, edition, format, seriesType);
		
		IFREnrouteCharts chartService = new IFREnrouteCharts(mockCycleClient);
		ProductSet productSet = (ProductSet) chartService.getIFREnrouteRelease(edition, format, geoname, seriesType).getEntity();
		
		assertNotNull(productSet, "ProductSet should not be null");
		assertNotNull(productSet.getEdition(), "Edition list should not be null");
		
		if (productSet.getStatus().getCode() == 200) {
			assertEquals(expectedCount, productSet.getEdition().size(), 
				String.format("Expected %d editions for %s/%s/%s/%s", expectedCount, edition, geoname, seriesType, format));
			
			for (Edition ed : productSet.getEdition()) {
				assertNotNull(ed.getProduct(), "Product should not be null for each edition");
				assertNotNull(ed.getEditionDate(), "Edition date should not be null");
				assertNotNull(ed.getEditionNumber(), "Edition number should not be null");
				
				String url = ed.getProduct().getUrl();
				if (url != null && !url.isEmpty()) {
					assertValidUrlFormat(url, geoname, seriesType, format);
				}
			}
		}
	}
	
	private void assertValidUrlFormat(String url, String geoname, String seriesType, String format) {
		assertTrue(url.contains("aeronav.faa.gov"), "URL should contain aeronav.faa.gov");
		assertTrue(url.contains("/enroute/"), "URL should contain /enroute/ path");
		assertTrue(url.endsWith(".zip"), "URL should end with .zip");
		
		String expectedPrefix = getExpectedFilePrefix(geoname, seriesType, format);
		if (expectedPrefix != null) {
			String filename = url.substring(url.lastIndexOf('/') + 1);
			assertTrue(filename.startsWith(expectedPrefix) || filename.contains(expectedPrefix),
				String.format("Filename %s should contain expected prefix %s", filename, expectedPrefix));
		}
	}
	
	private String getExpectedFilePrefix(String geoname, String seriesType, String format) {
		if ("US".equalsIgnoreCase(geoname)) {
			if ("TIFF".equalsIgnoreCase(format)) {
				if ("LOW".equalsIgnoreCase(seriesType)) return "enr_l";
				if ("HIGH".equalsIgnoreCase(seriesType)) return "enr_h";
				if ("AREA".equalsIgnoreCase(seriesType)) return "enr_a";
			} else if ("PDF".equalsIgnoreCase(format)) {
				if ("LOW".equalsIgnoreCase(seriesType)) return "elus";
				if ("HIGH".equalsIgnoreCase(seriesType)) return "ehus";
				if ("AREA".equalsIgnoreCase(seriesType)) return "area";
			}
		} else if ("ALASKA".equalsIgnoreCase(geoname)) {
			if ("TIFF".equalsIgnoreCase(format)) {
				if ("LOW".equalsIgnoreCase(seriesType)) return "enr_akl";
				if ("HIGH".equalsIgnoreCase(seriesType)) return "enr_akh";
			} else if ("PDF".equalsIgnoreCase(format)) {
				if ("LOW".equalsIgnoreCase(seriesType)) return "elak";
				if ("HIGH".equalsIgnoreCase(seriesType)) return "ehak";
			}
		} else if ("PACIFIC".equalsIgnoreCase(geoname)) {
			if ("TIFF".equalsIgnoreCase(format) && "HIGH".equalsIgnoreCase(seriesType)) return "enr_p";
			if ("PDF".equalsIgnoreCase(format) && "HIGH".equalsIgnoreCase(seriesType)) return "ephi";
		} else if ("CARIBBEAN".equalsIgnoreCase(geoname)) {
			if ("PDF".equalsIgnoreCase(format)) {
				if ("LOW".equalsIgnoreCase(seriesType)) return "elcb";
				if ("HIGH".equalsIgnoreCase(seriesType)) return "ehcb";
				if ("AREA".equalsIgnoreCase(seriesType)) return "elcb";
			}
		}
		return null;
	}
	
	@ParameterizedTest(name = "Test invalid parameters: edition={0}, format={1}, geoname={2}")
	@MethodSource("provideInvalidParameters")
	@DisplayName("IFR Enroute Invalid Parameter Test")
	void testInvalidParameters(String edition, String format, String geoname, String seriesType, int expectedStatusCode) {
		IFREnrouteCharts chartService = new IFREnrouteCharts(mockCycleClient);
		ProductSet productSet = (ProductSet) chartService.getIFREnrouteRelease(edition, format, geoname, seriesType).getEntity();
		
		assertNotNull(productSet, "ProductSet should not be null even for invalid parameters");
		assertEquals(expectedStatusCode, productSet.getStatus().getCode(), 
			"Status code should match expected for invalid parameters");
	}
	
	static Stream<Arguments> provideInvalidParameters() {
		return Stream.of(
			Arguments.of("INVALID_EDITION", "PDF", "US", "LOW", 400),
			Arguments.of("CURRENT", "INVALID_FORMAT", "US", "LOW", 400),
			Arguments.of("CURRENT", "PDF", null, "LOW", 404),
			Arguments.of("CURRENT", "PDF", "US", null, 404)
		);
	}
	
	@ParameterizedTest(name = "Test URL pattern for {0}/{1}/{2}/{3}")
	@MethodSource("provideTestParameters")
	@DisplayName("URL Pattern Validation Test")
	void testUrlPatternGeneration(String edition, String geoname, String seriesType, String format, int expectedCount) {
		String dateString = DATE_FORMAT.format(mockCycleElement.getChart_effective_date());
		String expectedUrlBase = "http://aeronav.faa.gov/enroute/" + dateString + "/";
		
		IFREnrouteCharts chartService = new IFREnrouteCharts(mockCycleClient);
		ProductSet productSet = (ProductSet) chartService.getIFREnrouteRelease(edition, format, geoname, seriesType).getEntity();
		
		if (productSet.getStatus().getCode() == 200) {
			for (Edition ed : productSet.getEdition()) {
				if (ed.getProduct() != null && ed.getProduct().getUrl() != null && !ed.getProduct().getUrl().isEmpty()) {
					String url = ed.getProduct().getUrl();
					assertTrue(url.startsWith(expectedUrlBase), 
						String.format("URL %s should start with %s", url, expectedUrlBase));
				}
			}
		}
	}
}
