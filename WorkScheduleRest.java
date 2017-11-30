package com.vistana.onsiteconcierge.rest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.WorkScheduleDto;
import com.vistana.onsiteconcierge.core.model.WorkSchedule;
import com.vistana.onsiteconcierge.core.model.WorkScheduleId;
import com.vistana.onsiteconcierge.core.service.WorkScheduleService;

@RestController
public class WorkScheduleRest {

	@Autowired
	protected WorkScheduleService service;

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/workSchedules")
	public List<WorkScheduleDto> getAllPersonWorkSchedule(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		return service.findByOrganizationAndProperty(organization, property).stream().map(p -> new WorkScheduleDto(p))
				.collect(Collectors.toList());

	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping("/workSchedule/{personId}")
	public List<WorkScheduleDto> updatePersonWorkSchedule(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @PathVariable Integer personId,
			@RequestBody Set<WorkScheduleDto> entities) {

		List<WorkSchedule> toDelete = service.findWithPersonId(organization, property, personId);

		List<WorkSchedule> updates = entities.stream().filter((ls) -> {
			return ls != null;
		}).map((ls) -> {
			WorkSchedule ws = new WorkSchedule(ls);
			WorkScheduleId id = ws.getId();
			id.setOrganizationId(organization);
			id.setPropertyId(property);
			ws.setId(id);
			return ws;
		}).collect(Collectors.toList());

		service.delete(toDelete);
		service.save(updates);

		return service.findByOrganizationAndProperty(organization, property).stream().map(p -> new WorkScheduleDto(p))
				.collect(Collectors.toList());

	}

}
