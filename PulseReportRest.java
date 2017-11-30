package com.vistana.onsiteconcierge.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.PulseGoalsDto;
import com.vistana.onsiteconcierge.core.service.PulseGoalsService;

@RestController
@RequestMapping("/pulsereport")
public class PulseReportRest {

	@Autowired
	private PulseGoalsService pulseReportService;

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/get")
	public List<PulseGoalsDto> getAllReportValues(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		List<PulseGoalsDto> pulseReports = pulseReportService.findByOrganizationIdAndPropertyIdQuery(organization,
				property);
		return pulseReports;
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping("/update")
	public List<PulseGoalsDto> updateReportValues(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property,
			@RequestBody List<PulseGoalsDto> body) {

		pulseReportService.updatePulseGoalsDto(body);
		return this.getAllReportValues(organization, property);
	}

}
