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

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.api.IFREnrouteCharts;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleData;

public class IFREnrouteChartsTest {
	private static final Logger logger = LoggerFactory
			.getLogger(IFREnrouteChartsTest.class);
	private IFREnrouteCharts chats;

	@BeforeAll
	public static void setup() {
		ChartCycleClient client = new ChartCycleClient();
		ChartCycleData cycle = client.getChartCycle(new Date(System.currentTimeMillis()), true);
		logger.info("Updated chart cycle in prep for IFR Enroute tests " + cycle.getName());
	}
	
	@BeforeEach
	public void initialize() {
		chats = new IFREnrouteCharts();
	}

	/**
	 * testDownloadOperations
	 */
	@Test
	public void testDownloadOperations() {
		
		ProductSet current = (ProductSet) chats.getIFREnrouteRelease("current", "tiff", "US", "low").getEntity();
		if (current.getEdition().size() > 0)
			logger.info(current.getEdition().get(0).getProduct().getUrl());
		assertEquals(Integer.valueOf(current.getStatus().getCode()), Integer.valueOf(200));
	}

	/**
	 * testEditionOperations
	 */
	@Test
	public void testEditionOperations() {
		
		ProductSet current = (ProductSet) chats.getIFREnrouteEdition("current").getEntity();
		assertEquals(Integer.valueOf(current.getStatus().getCode()), Integer.valueOf(
				200));

		ProductSet next = (ProductSet) chats.getIFREnrouteEdition("Next").getEntity();
		assertEquals(Integer.valueOf(next.getStatus().getCode()), Integer.valueOf(200));
		
		
	}
}
