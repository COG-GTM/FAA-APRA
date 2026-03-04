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
package gov.faa.ait.apra.cycle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;

import gov.faa.ait.apra.bootstrap.Config;

/**
 * 
 *
 */


public class VFRChartCycleClient {
	private static ChartCycleData cycle;
	private static Date lastUpdate;
	private Date today;
	private static String chartCycleTypeCode;
	private static final Logger logger = LoggerFactory
			.getLogger(VFRChartCycleClient.class);

	/**
	 * 
	 * @param typeCode
	 */
	public VFRChartCycleClient(String typeCode) {
		this.today = new Date(System.currentTimeMillis());

		// specify the type code desired. For the Gulf of Mexico charts, this is
		// the type code of IFR_PGOM
		setChartCycleTypeCode (typeCode);

		if (this.isUpdateRequired()) {
			setLastUpdate();
			setChartCycle(getChartCycle());
		}
	}

	/**
	 * dd
	 */

	public VFRChartCycleClient() {
		this.today = new Date(System.currentTimeMillis());
		// default to the Grand Canyon type code to avoid breaking Grand Canyon
		// service
		setChartCycleTypeCode("Grand_Canyon");

		if (this.isUpdateRequired()) {
			setLastUpdate();
			setChartCycle(getChartCycle());
		}
	}

	/**
	 * 
	 * @return
	 */

	public ChartCycleData getChartCycle() {
		return getChartCycle(today, false);
	}

	/**
	 * dd
	 */
	public void forceUpdate() {
		
		logger.debug("VFR Chart cycle forceUpdate() was called.");
		
		if (today == null) 
			today = new Date(System.currentTimeMillis());
		
		getChartCycle(today, true);
	}

	/**
	 * 
	 * @param forceUpdate
	 * @return
	 */
	public ChartCycleData getChartCycle(boolean forceUpdate) {
		logger.debug("VFR Chart cycle getChartCycle({}) called.", forceUpdate);
		
		return getChartCycle(today, forceUpdate);
	}

	/**
	 * 
	 * @param targetDate
	 * @param forceUpdate
	 * @return
	 */
	public ChartCycleData getChartCycle(Date targetDate, boolean forceUpdate) {
		ChartCycleData cycleData;
		StringBuilder url = new StringBuilder();

		if (forceUpdate) {
			logger.debug("VFR Chart cycle force update is true. Setting lastUpdate and cycle to null.");
			setLastUpdate();
			setChartCycle(null);
		}
		
		url = url.append(Config.getDenodoHost()+Config.getDenodoVFRCycleResource());
		SimpleDateFormat formatter = new SimpleDateFormat ("MM/dd/yyyy");
		
		String dateString = formatter.format(targetDate);

		StringBuilder queryString = new StringBuilder();
		queryString = queryString.append("?query_date=" + dateString);
		queryString = queryString.append("&chart_cycle_type_code="
				+ VFRChartCycleClient.chartCycleTypeCode);
		queryString = queryString.append("&%24format=json");

		url = url.append(queryString);

		if (cycle != null && lastUpdate != null) {
			logger.info("VFR Chart cycle already available. Returning cycle without round trip to denodo server.");
			return cycle;
		} 
		else {
			logger.info("VFR Chart cycle or lastUpdate is null. Continuing with cycle retrieval.");			
		}

		setLastUpdate();

		String unbound = "";

		try {
			logger.info("Calling denodo for vfr chart cycle at {}", url);
			Client client = ClientBuilder.newClient();

			WebTarget webTarget = client.target(url.toString());
			long now = System.currentTimeMillis();
			unbound = webTarget.request(MediaType.APPLICATION_XML_TYPE).get(
					String.class);
			long duration = System.currentTimeMillis() - now;
			logger.info("Call for chart cycle from denodo server took {} ms", duration);
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
			cycleData = mapper.readValue(unbound.getBytes(StandardCharsets.UTF_16), ChartCycleData.class);
			setChartCycle(cycleData);
		} catch (Exception ex) {
			logger.error("getChartCycle", ex);
			setChartCycle(null);
			return null;
		}
		return cycle;
	}

	private boolean isUpdateRequired() {
		if (lastUpdate == null || cycle == null) {
			logger.info("VFR chart cycle update required. Either last update or cycle was null and needs to be refreshed.");
			return true;
		}

		long diff = today.getTime() - lastUpdate.getTime();
		
		logger.debug("Age of VFR cycle is {} ms", diff);
		
		long hours = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
		
		boolean updateRequired = hours >= Config.getCycleAgeLimit();
		
		if (updateRequired) {
			logger.debug("VFR chart cycle update is required due to expiration. VFR Chart cycle is {} hours old. Expiration period is {}", hours, Config.getCycleAgeLimit());
		}
		else {
			logger.debug("VFR chart cycle update is NOT required due to expiration. VFR Chart cycle is {} hours old. Expiration period is {}", hours, Config.getCycleAgeLimit());
		}
		
		return updateRequired;
	}

	private ChartCycleElementsJson getCycle(String periodCode) {
		if (this.isUpdateRequired()) {
			logger.info("VFR Chart cycle requires an update. Updating cache.");
			getChartCycle();
		}

		boolean found;

		ChartCycleElementsJson[] elements = cycle.getElements();
		for (ChartCycleElementsJson element : elements) {
			found = element.getChart_cycle_period_code().equalsIgnoreCase(
					periodCode);
			if (found) {
				logger.info("Found VFR chart cycle in cache. Returning {} chart cycle.", periodCode);
				return element;
			}
		}
		logger.warn("VFR chart cycle {} not found in cache. Returning null.", periodCode);
		return null;
	}

	public ChartCycleElementsJson getNextCycle() {

		return getCycle("NEXT");
	}

	public ChartCycleElementsJson getCurrentCycle() {
		return getCycle("CURRENT");
	}
	
	private void setChartCycleTypeCode (String typeCode) {
		logger.debug("chartCycleTypeCode == {} and typeCode == {}", VFRChartCycleClient.chartCycleTypeCode, typeCode);
		
		if (VFRChartCycleClient.chartCycleTypeCode != null && VFRChartCycleClient.chartCycleTypeCode.equalsIgnoreCase(typeCode)) {
			return;
		}

		VFRChartCycleClient.chartCycleTypeCode = typeCode;
		forceUpdate();
	}

	public void setLastUpdate () {
		VFRChartCycleClient.lastUpdate = new Date (System.currentTimeMillis());
	}
	
	public void setChartCycle(ChartCycleData data) {
		VFRChartCycleClient.cycle = data;
	}
}
