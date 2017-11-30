package com.vistana.onsiteconcierge.rest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.ActivatorDto;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.model.Activator;
import com.vistana.onsiteconcierge.core.model.IRankable;
import com.vistana.onsiteconcierge.core.service.ActivatorService;

@RestController
public class ActivatorRest {

	private static final Boolean ACTIVE_FLAG = true;

	@Autowired
	protected ActivatorService service;

	@PreAuthorize(Constants.ALLOWED_FOR_CHANGE_USER_ACCESS)
	@PostMapping("/activator/batch")
	public List<ActivatorDto> batchUpdate(
			@RequestParam(value = CoreConstants.ORGANIZATION, required = true) String organization,
			@RequestParam(value = CoreConstants.PROPERTY, required = true) int property,
			@Valid @RequestBody Set<ActivatorDto> entities, BindingResult result) {

		if (result.hasErrors()) {
			for (FieldError error : result.getFieldErrors()) {
				String err = "error: {} in field :" + error.getField() + ", " + error.getObjectName() + " "
						+ error.getDefaultMessage();
				throw new InvalidClientRequest("Exception Caught: " + err);
			}
		}

		List<Activator> saved = service.findByOrganizationIdAndPropertyId(organization, property).stream()
				.collect(Collectors.toList());
		List<Activator> updates = entities.stream().filter((ls) -> {
			return ls != null;
		}).map((ls) -> {
			return new Activator(ls);
		}).collect(Collectors.toList());

		int sortOrderCorrection = 0;
		for (Activator entity : updates) {
			entity.setOrganizationId(organization);
			entity.setPropertyId(property);

			// Objects that don't currently exist cannot be updated
			if (!saved.contains(entity)) {
				throw new InvalidClientRequest();
			}

			Activator old = saved.get(saved.indexOf(entity));

			// Adding in the sort order for anything missing it or anything
			// going from an active state to inactive state
			if (entity.getSortOrder() == null || entity.getSortOrder() > saved.size()) {
				entity.setSortOrder(saved.size() - sortOrderCorrection);
			} else if (old.getActiveFlag() && !entity.getActiveFlag()) {
				entity.setSortOrder(saved.size() - sortOrderCorrection);
				sortOrderCorrection++;
			}
		}

		Set<Activator> sorted = IRankable.set(saved, updates);
		service.save(sorted);

		return service.findByOrganizationIdAndPropertyId(organization, property).stream().map((ls) -> {
			return new ActivatorDto(ls);
		}).collect(Collectors.toList());
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/activators")
	public List<ActivatorDto> getAllActivatorValues(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		List<Activator> activators = service
				.findByOrganizationIdAndPropertyIdAndActiveFlagOrderBySortOrder(organization, property, ACTIVE_FLAG);
		return activators.stream().map(p -> new ActivatorDto(p)).collect(Collectors.toList());
	}

	@PreAuthorize(Constants.ALLOWED_FOR_CHANGE_USER_ACCESS)
	@PostMapping("/activators")
	public List<ActivatorDto> updateAllActivators(
			@RequestParam(value = CoreConstants.ORGANIZATION, required = true) String organization,
			@RequestParam(value = CoreConstants.PROPERTY, required = true) int property,
			@Valid @RequestBody ActivatorDto entity, BindingResult result) {

		if (result.hasErrors()) {
			for (FieldError error : result.getFieldErrors()) {
				String err = "error: {} in field :" + error.getField() + ", " + error.getObjectName() + " "
						+ error.getDefaultMessage();
				throw new InvalidClientRequest("Exception Caught: " + err);
			}
		}
		List<Activator> activators = service.findByOrganizationIdAndPropertyId(organization, property).stream()
				.collect(Collectors.toList());

		Activator create = new Activator(entity);
		if (activators.contains(create)) {
			throw new InvalidClientRequest();
		}

		create.setOrganizationId(organization);
		create.setPropertyId(property);

		if (!create.getActiveFlag()) {
			create.setSortOrder(activators.size() + 1);
		}

		List<Activator> updateList = IRankable.add(activators, create);
		service.save(updateList);

		return service.findByOrganizationIdAndPropertyId(organization, property).stream().map((ls) -> {
			return new ActivatorDto(ls);
		}).collect(Collectors.toList());
	}

}
