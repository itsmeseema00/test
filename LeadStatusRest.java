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
import com.vistana.onsiteconcierge.core.dto.LeadStatusDto;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.model.IRankable;
import com.vistana.onsiteconcierge.core.model.LeadStatus;
import com.vistana.onsiteconcierge.core.service.LeadStatusService;

@RestController
@PreAuthorize(Constants.ALLOWED_FOR_ADMINISTRATOR)
public class LeadStatusRest {

	private static final Boolean ACTIVE_FLAG = true;

	@Autowired
	private LeadStatusService service;

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping("/leadstatus/batch")
	public List<LeadStatusDto> batchUpdate(
			@RequestParam(value = CoreConstants.ORGANIZATION, required = true) String organization,
			@RequestParam(value = CoreConstants.PROPERTY, required = true) int property,
			@RequestBody Set<LeadStatusDto> entities) {

		List<LeadStatus> saved = service.findAllByOrganizationIdAndPropertyId(organization, property).stream()
				.collect(Collectors.toList());
		List<LeadStatus> updates = entities.stream().filter((ls) -> {
			return ls != null;
		}).map((ls) -> {
			return new LeadStatus(ls);
		}).collect(Collectors.toList());

		int sortOrderCorrection = 0;
		for (LeadStatus entity : updates) {
			if (!entity.getId().getOrganizationId().equals(organization)
					|| !entity.getId().getPropertyId().equals(property)) {
				throw new InvalidClientRequest();
			}

			// Objects that don't currently exist cannot be updated
			if (!saved.contains(entity)) {
				throw new InvalidClientRequest();
			}

			LeadStatus old = saved.get(saved.indexOf(entity));

			// Adding in the sort order for anything missing it or anything
			// going from an active state to inactive state
			if (entity.getSortOrder() == null || entity.getSortOrder() > saved.size()) {
				entity.setSortOrder(saved.size() - sortOrderCorrection);
			} else if (old.getActiveFlag() && !entity.getActiveFlag()) {
				entity.setSortOrder(saved.size() - sortOrderCorrection);
				sortOrderCorrection++;
			}
		}

		Set<LeadStatus> sorted = IRankable.set(saved, updates);

		service.save(sorted);

		return service.findAllByOrganizationIdAndPropertyId(organization, property).stream().map((ls) -> {
			return new LeadStatusDto(ls);
		}).collect(Collectors.toList());
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/leadstatus")
	public List<LeadStatusDto> getAll(
			@RequestParam(value = CoreConstants.ORGANIZATION, required = true) String organization,
			@RequestParam(value = CoreConstants.PROPERTY, required = true) int property,
			@RequestParam(value = "showActive", required = false) Boolean showActive) {
		if (showActive != null && showActive) {
			return service.findAllByOrganizationIdAndPropertyId(organization, property, ACTIVE_FLAG).stream()
					.map((ls) -> {
						return new LeadStatusDto(ls);
					}).collect(Collectors.toList());
		} else {
			return service.findAllByOrganizationIdAndPropertyId(organization, property).stream().map((ls) -> {
				return new LeadStatusDto(ls);
			}).collect(Collectors.toList());
		}
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/leadstatus/flag/{leadStatusCode}")
	public LeadStatusDto getOneLeadStatus(
			@RequestParam(value = CoreConstants.ORGANIZATION, required = true) String organization,
			@RequestParam(value = CoreConstants.PROPERTY, required = true) int property,
			@PathVariable String leadStatusCode) {

		LeadStatus leadStatus = service.findAllByOrganizationIdAndPropertyIdLeadStatusCode(organization, property,
				leadStatusCode);

		return new LeadStatusDto(leadStatus);
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping("/leadstatus")
	public List<LeadStatusDto> save(@RequestParam(value = "organization", required = true) String organization,
			@RequestParam(value = "property", required = true) int property, @RequestBody LeadStatusDto entity) {

		entity.setOrganizationId(organization);
		entity.setPropertyId(property);
		LeadStatus create = new LeadStatus(entity);

		List<LeadStatus> statuses = service.findAllByOrganizationIdAndPropertyId(organization, property).stream()
				.collect(Collectors.toList());

		if (statuses.contains(create)) {
			throw new InvalidClientRequest();
		}

		if (!create.getActiveFlag()) {
			create.setSortOrder(statuses.size() + 1);
		}

		List<LeadStatus> updateList = IRankable.add(statuses, create);

		service.save(updateList);
		return service.findAllByOrganizationIdAndPropertyId(organization, property).stream().map((ls) -> {
			return new LeadStatusDto(ls);
		}).collect(Collectors.toList());
	}

}
