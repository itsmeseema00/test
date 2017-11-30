package com.vistana.onsiteconcierge.rest;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.vistana.onsiteconcierge.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vistana.onsiteconcierge.config.service.TourBookService;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.service.AccessService;

@RestController
public class HealthCheckRest {

	private static String KOR = "19";

	private static Logger logger = LoggerFactory.getLogger(HealthCheckRest.class);

	private static long MIN_MEMORY = 512;

	private static boolean OVERRIDE = false;

	@Autowired
	protected AccessService accessService;

	@Autowired
	private Environment environment;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Value("${com.vistana.node:DEFAULT}")
	private String nodeName;

	@Autowired
	private TourBookService tourBookService;

	@GetMapping(value = "/status")
	public ResponseEntity<String> getHealthJvm() {

		if (HealthCheckRest.OVERRIDE) {
			return new ResponseEntity<String>(CoreConstants.UNSUCCESSFUL, HttpStatus.OK);
		}

		long maxMem = Runtime.getRuntime().maxMemory();
		if (maxMem < .85 * MIN_MEMORY) {
			logger.info("Heap limit is too low (" + (maxMem / (1024 * 1024))
					+ "MB), please increase heap size at least up to " + MIN_MEMORY + "MB.");
			return new ResponseEntity<String>(CoreConstants.UNSUCCESSFUL, HttpStatus.OK);
		} else {
			return new ResponseEntity<String>(CoreConstants.SUCCESSFUL, HttpStatus.OK);
		}
	}


	@GetMapping(value="/version")
	public Map<String, String> getAppVersion(){
		Map<String, String>  map  = new HashMap<>();
		map.put("appName", applicationProperties.getApplicationName());
		map.put("revision", applicationProperties.getBuildChecksum());
		map.put("buildTime", applicationProperties.getBuildTime());
		map.put("version", applicationProperties.getVersion());
		map.put("node_name", nodeName);
		map.put("environment", Arrays.toString(environment.getActiveProfiles()));
		return map;
	}


	@GetMapping(value = "/node")
	public HashMap<String, Object> getNodeInformation() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("nodeName", this.nodeName);
		return map;
	}

	@GetMapping(value = "/health")
	public ResponseEntity<String> healthDatabaseAndServices() {

		try {
			// Test ConciergeApp Data base
			accessService.findAll().stream().collect(Collectors.toList());
		} catch (Exception e) {
			logger.error("ConciergeApp DB - not available", e);
			return new ResponseEntity<String>(CoreConstants.UNSUCCESSFUL, HttpStatus.OK);
		}

		try {
			// Test Vistana Services
			tourBookService.findTourAvailability(KOR, null, new Date(), new Date());
		} catch (Exception e) {
			logger.error("Vistana Services - not available", e);
			return new ResponseEntity<String>(CoreConstants.UNSUCCESSFUL, HttpStatus.OK);
		}

		return new ResponseEntity<String>(CoreConstants.SUCCESSFUL, HttpStatus.OK);
	}

	@PostMapping(value = "/status")
	public ResponseEntity<String> postHealthJvm(@RequestParam(value = "enabled", required = true) Boolean override) {

		HealthCheckRest.OVERRIDE = override;
		return new ResponseEntity<String>(CoreConstants.SUCCESSFUL, HttpStatus.OK);
	}

}
