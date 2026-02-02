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

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.faa.ait.apra.bootstrap.Config;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.TACCycleClient;
import gov.faa.ait.apra.util.URLCache;
import gov.faa.ait.apra.cycle.VFRChartCycleClient;
import gov.faa.ait.apra.cycle.WallPlanningChartCycleClient;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/management")
@Tag(name = "Management Control", description = "Management and control functions for configuration reload, cache flush, start, stop, and health")
public class ManagementControl {
	private static int mode = 1;

	@GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
	public String getStatus() {
		if (ManagementControl.mode == 0)
			return "ServerDown";

		return "ServerOK";
	}

	@GetMapping(value = "/stop", produces = MediaType.TEXT_PLAIN_VALUE)
	public static String stop() {
		ManagementControl.mode = 0;
		return "ServerDown";
	}

	@GetMapping(value = "/start", produces = MediaType.TEXT_PLAIN_VALUE)
	public static String start() {
		ManagementControl.mode = 1;
		return "ServerOK";
	}

	@GetMapping(value = "/flush", produces = MediaType.TEXT_PLAIN_VALUE)
	public String refresh() {
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

	@GetMapping(value = "/config", produces = MediaType.TEXT_PLAIN_VALUE)
	public String reloadConfig() {
		Config.loadConfig();
		return "Config Reload Complete";
	}
}
