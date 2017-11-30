package com.vistana.onsiteconcierge.rest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.GuestTypeDataDto;
import com.vistana.onsiteconcierge.core.dto.LookUpDto;
import com.vistana.onsiteconcierge.core.dto.RateDto;
import com.vistana.onsiteconcierge.core.exception.GenericException;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.exception.SecurityAccessException;
import com.vistana.onsiteconcierge.core.model.Rate;
import com.vistana.onsiteconcierge.core.service.LookupService;
import com.vistana.onsiteconcierge.core.service.RateService;

@RestController
public class GuestTypeRest {

	@Autowired
	private LookupService lookupService;

	@Autowired
	private RateService rateService;

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping("/guesttype/batch")
	public List<RateDto> batchCreateGuestTypeMapping(
			@RequestParam(value = CoreConstants.ORGANIZATION, required = true) String organization,
			@RequestParam(value = CoreConstants.PROPERTY, required = true) Integer property,
			@RequestBody List<RateDto> dtos) {

		try {
			List<String> guestTypes = lookupService.getLookUpData(organization, LookupService.GUEST_TYPE, property)
					.stream().map(l -> l.getId().getLookupCode()).collect(Collectors.toList());

			Set<Rate> rates = new HashSet<>();
			dtos.forEach((dto) -> {
				if (!guestTypes.contains(dto.getGuestTypeCode())) {
					throw new InvalidClientRequest();
				} else if (!dto.getOrganizationId().equals(organization) || !dto.getPropertyId().equals(property)) {
					throw new SecurityAccessException();
				}
				rates.add(new Rate(dto));
			});

			this.rateService.save(rates);
			return rateService.findAllByOrganizationAndProperty(organization, property).stream().map(RateDto::new)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new GenericException("Exception Caught: " + e.getMessage());
		}

	}

	@PreAuthorize(Constants.ALLOWED_FOR_CHANGE_GUEST_TYPE)
	@PostMapping("/guesttype")
	public RateDto createGuestTypeMapping(
			@RequestParam(value = CoreConstants.ORGANIZATION, required = true) String organization,
			@RequestParam(value = CoreConstants.PROPERTY, required = true) Integer property, @RequestBody RateDto dto) {

		try {
			List<String> guestTypes = lookupService.getLookUpData(organization, LookupService.GUEST_TYPE, property)
					.stream().map(l -> l.getId().getLookupCode()).collect(Collectors.toList());

			if (!guestTypes.contains(dto.getGuestTypeCode())) {
				throw new InvalidClientRequest();
			} else if (!dto.getOrganizationId().equals(organization) || !dto.getPropertyId().equals(property)) {
				throw new SecurityAccessException();
			}

			Rate saved = this.rateService.save(new Rate(dto));
			return new RateDto(saved);
		} catch (Exception e) {
			throw new GenericException("Exception Caught: " + e.getMessage());
		}

	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/guesttype")
	public GuestTypeDataDto getCurrentMappings(
			@RequestParam(value = CoreConstants.ORGANIZATION, required = true) String organization,
			@RequestParam(value = CoreConstants.PROPERTY, required = true) Integer property) {

		try {
			List<LookUpDto> guestTypes = lookupService.getLookUpData(organization, LookupService.GUEST_TYPE, property)
					.stream().map(LookUpDto::new).collect(Collectors.toList());

			List<RateDto> rates = rateService.findAllByOrganizationAndProperty(organization, property).stream()
					.map(RateDto::new).collect(Collectors.toList());

			return new GuestTypeDataDto(rates, guestTypes);
		} catch (Exception e) {
			throw new GenericException("Exception Caught: " + e.getMessage());
		}
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/search/guesttype")
	public List<RateDto> getGuestTypeMappings(
			@RequestParam(value = CoreConstants.ORGANIZATION, required = true) String organization,
			@RequestParam(value = CoreConstants.PROPERTY, required = true) int property,
			@RequestParam(value = CoreConstants.START_DATE, required = true) @DateTimeFormat(pattern = CoreConstants.DATE_FORMAT) Date start,
			@RequestParam(value = CoreConstants.END_DATE, required = true) @DateTimeFormat(pattern = CoreConstants.DATE_FORMAT) Date end) {

		try {
			if (ChronoUnit.DAYS.between(start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
					end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) > 90
					|| LocalDate.now().isAfter(start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())) {
				throw new InvalidClientRequest();
			}

			return this.rateService.getRatesForGuestTypeMapping(organization, property, start, end).stream()
					.map(RateDto::new).collect(Collectors.toList());

		} catch (Exception e) {
			throw new GenericException("Exception Caught: " + e.getMessage());
		}
	}
}
