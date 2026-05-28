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
package gov.faa.ait.apra.api.management;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.bootstrap.Config;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.TACCycleClient;
import gov.faa.ait.apra.security.AuditLogger;
import gov.faa.ait.apra.util.URLCache;
import gov.faa.ait.apra.cycle.VFRChartCycleClient;
import gov.faa.ait.apra.cycle.WallPlanningChartCycleClient;

@Path("/management")
/**
 * This class provides management and control functions to assist with configuration reload, cache flush, start, stop, and health.
 * STIG V-220629: Management endpoints require authentication via AuthenticationFilter.
 * STIG V-220635: All management actions are audit-logged.
 * @author FAA
 *
 */
public class ManagementControl {
	private static final Logger logger = LoggerFactory.getLogger(ManagementControl.class);
	private static int mode = 1;

	@Context
	private HttpServletRequest servletRequest;

	@Path("/health")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
	public String getStatus () {
		if (ManagementControl.mode == 0) 
			return "ServerDown";
		
		return "ServerOK";
	}
	
	@Path("/stop") 
    @GET
    @Produces(MediaType.TEXT_PLAIN)
	public String stop(@Context HttpHeaders headers) {
		AuditLogger.getInstance().logAdminAction(
			getClientIp(headers), "server_stop");
		logger.warn("Management: Server stop requested");
		ManagementControl.mode = 0;
		return "ServerDown";
	}
	
	@Path("/start") 
    @GET
    @Produces(MediaType.TEXT_PLAIN)
	public String start(@Context HttpHeaders headers) {
		AuditLogger.getInstance().logAdminAction(
			getClientIp(headers), "server_start");
		logger.info("Management: Server start requested");
		ManagementControl.mode = 1;
		return "ServerOK";
	}
	
	@Path("/flush")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
	public String refresh(@Context HttpHeaders headers) {
		AuditLogger.getInstance().logAdminAction(
			getClientIp(headers), "cache_flush");
		logger.info("Management: Cache flush requested");

		ChartCycleClient cycleClient = new ChartCycleClient();
		cycleClient.forceUpdate();
		TACCycleClient tacCycleClient = new TACCycleClient();
		tacCycleClient.forceUpdate();
		VFRChartCycleClient vfrClient = new VFRChartCycleClient();
		vfrClient.forceUpdate();
		WallPlanningChartCycleClient wpClient = new WallPlanningChartCycleClient();
		wpClient.forceUpdate();
		
		URLCache.getInstance().flush();
		
		return "Cycle Reload Complete";
	}

	@Path("/config")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
	public String reloadConfig(@Context HttpHeaders headers) {
		AuditLogger.getInstance().logAdminAction(
			getClientIp(headers), "config_reload");
		logger.info("Management: Config reload requested");
		Config.loadConfig();		
		return "Config Reload Complete";
	}

	private String getClientIp(HttpHeaders headers) {
		if (headers != null) {
			String forwarded = headers.getHeaderString("X-Forwarded-For");
			if (forwarded != null && !forwarded.isEmpty()) {
				return forwarded.split(",")[0].trim();
			}
		}
		if (servletRequest != null) {
			String remoteAddr = servletRequest.getRemoteAddr();
			if (remoteAddr != null && !remoteAddr.isEmpty()) {
				return remoteAddr;
			}
		}
		return "0.0.0.0";
	}
}
