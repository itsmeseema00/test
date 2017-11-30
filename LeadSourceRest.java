package com.vistana.onsiteconcierge.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.DropDownDto;
import com.vistana.onsiteconcierge.core.dto.LeadSourceDataDto;
import com.vistana.onsiteconcierge.core.dto.LookUpDto;
import com.vistana.onsiteconcierge.core.dto.RateDto;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.model.LeadSourceId;
import com.vistana.onsiteconcierge.core.model.Rate;
import com.vistana.onsiteconcierge.core.service.LeadSourceService;
import com.vistana.onsiteconcierge.core.service.LookupService;
import com.vistana.onsiteconcierge.core.service.RateService;

@RestController
public class LeadSourceRest {

	@Autowired
	private LookupService lsService;

	@Autowired
	private RateService rateService;

	@Autowired
	private LeadSourceService service;

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping("/leadsource")
	public Set<RateDto> create(@RequestParam(value = CoreConstants.ORGANIZATION, required = true) String organization,
			@RequestParam(value = CoreConstants.PROPERTY, required = true) Integer property,
			@RequestBody RateDto entity) {

		entity.setPropertyId(property);
		entity.setOrganizationId(organization);

		Rate rate = new Rate(entity);
		List<Rate> rs = rateService.findAll(organization, property).stream().collect(Collectors.toList());

		if (!rs.contains(rate)) {
			throw new InvalidClientRequest();
		} else if (service.find(new LeadSourceId(organization, property, rate.getLeadSourceCode())) == null) {
			throw new InvalidClientRequest();
		}

		Rate oldR = rs.get(rs.indexOf(rate));

		rate.setOverride(oldR.getOverride());
		rate.setGuestTypeCode(oldR.getGuestTypeCode());
		rate.setRateName(oldR.getRateName());

		this.rateService.save(rate);

		return this.getDisplayRates(organization, property);
	}

	private Set<RateDto> getDisplayRates(String organization, Integer property) {

		Set<RateDto> rates = rateService.findAll(organization, property).stream().map(r -> new RateDto(r, false))
				.collect(Collectors.toSet());

		return rates.stream().filter((r) -> r.getLeadSourceCode() != null).collect(Collectors.toSet());
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/leadsource")
	public LeadSourceDataDto leadSourceList(
			@RequestParam(value = CoreConstants.ORGANIZATION, required = true) String organization,
			@RequestParam(value = CoreConstants.PROPERTY, required = true) Integer property) {

		Set<RateDto> rates = rateService.findAll(organization, property).stream().map(r -> new RateDto(r, false))
				.collect(Collectors.toSet());

		Set<RateDto> displayRates = rates.stream().filter(r -> r.getLeadSourceCode() != null)
				.collect(Collectors.toSet());

		Set<RateDto> unmappedRates = rates.stream().filter(r -> r.getLeadSourceCode() == null)
				.collect(Collectors.toSet());

		List<LookUpDto> guestTypes = this.lsService.getLookUpData(organization, LookupService.GUEST_TYPE, property)
				.stream().map((lk) -> {
					return new LookUpDto(lk);
				}).collect(Collectors.toList());

		Set<DropDownDto> dropDownLeadSource = service.findAll(organization, property).stream().map(lS -> {
			return new DropDownDto(lS.getId().getLeadSourceCode(),
					lS.getId().getLeadSourceCode().concat(" - ").concat(lS.getLeadSourceDesc()));
		}).collect(Collectors.toSet());

		return new LeadSourceDataDto(displayRates, dropDownLeadSource, guestTypes, unmappedRates);
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping("/leadsource/leadsourcecode")
	public Set<RateDto> removeLeadSource(
			@RequestParam(value = CoreConstants.ORGANIZATION, required = true) String organization,
			@RequestParam(value = CoreConstants.PROPERTY, required = true) Integer property,
			@RequestBody RateDto entity) {

		Rate rate = new Rate(entity);
		List<Rate> rs = rateService.findAll(organization, property).stream().collect(Collectors.toList());

		if (!rs.contains(rate)) {
			throw new InvalidClientRequest();
		} else if (service.find(new LeadSourceId(organization, property, rate.getLeadSourceCode())) == null) {
			throw new InvalidClientRequest();
		}

		rate.setLeadSourceCode(null);

		this.rateService.save(rate);

		return this.getDisplayRates(organization, property);

	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping("/leadsource/batch")
	public Set<RateDto> updateBatch(
			@RequestParam(value = CoreConstants.ORGANIZATION, required = true) String organization,
			@RequestParam(value = CoreConstants.PROPERTY, required = true) Integer property,
			@RequestBody List<RateDto> entities) {

		List<Rate> incomingUpdates = entities.stream().map(Rate::new).collect(Collectors.toList());
		List<Rate> rates = this.getDisplayRates(organization, property).stream().map(Rate::new)
				.collect(Collectors.toList());
		List<Rate> savedUpdates = new ArrayList<Rate>();

		for (Rate rate : incomingUpdates) {
			if (rates.contains(rate)) {
				int indx = rates.indexOf(rate);
				Rate oldRate = rates.get(indx);
				oldRate.setLeadSourceCode(rate.getLeadSourceCode());
				savedUpdates.add(oldRate);
				rates.set(indx, oldRate);
			} else {
				throw new InvalidClientRequest();
			}
		}

		this.rateService.save(savedUpdates);

		return this.getDisplayRates(organization, property);
	}

}
