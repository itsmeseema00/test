package com.vistana.onsiteconcierge.rest;

import java.util.List;
import java.util.Optional;
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
import com.vistana.onsiteconcierge.core.dto.LocationDataDto;
import com.vistana.onsiteconcierge.core.dto.LocationDto;
import com.vistana.onsiteconcierge.core.dto.LookUpDto;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.model.Lookup;
import com.vistana.onsiteconcierge.core.model.OnSiteMarketingLocation;
import com.vistana.onsiteconcierge.core.service.LookupService;
import com.vistana.onsiteconcierge.core.service.OnSiteMarketingLocationService;

@RestController
@PreAuthorize(Constants.ALLOWED_FOR_ADMINISTRATOR)
public class OnSiteMarketingLocationRest {

	@Autowired
	protected LookupService lookupService;

	@Autowired
	protected OnSiteMarketingLocationService service;

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping("/location")
	public List<LocationDto> create(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @RequestBody LocationDto dto) {

		List<OnSiteMarketingLocation> locations = this.service.findAll(organization, property);

		List<Lookup> locationTypes = lookupService.getLookUpData(organization, LookupService.LOCATION_TYPE, property);
		Set<LookUpDto> dtoTypes = locationTypes.stream().map(LookUpDto::new).collect(Collectors.toSet());

		Optional<LookUpDto> type = dtoTypes.stream().filter(lT -> lT.getLookUpCode().equals(dto.getLocationTypeCode()))
				.findAny();

		if (!type.isPresent()) {
			throw new InvalidClientRequest(); // Location type does not exist
		}

		dto.setActiveFlag(true);
		dto.setSortOrder(locations.size() + 1);
		dto.setOnsiteMarketingLocationId(locations.size() + 1);
		dto.setPropertyId(property);
		dto.setOrganizationId(organization);

		OnSiteMarketingLocation location = new OnSiteMarketingLocation(dto);
		this.service.save(location);

		locations.add(location);
		return locations.stream().map(LocationDto::new).collect(Collectors.toList());
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/location")
	public LocationDataDto getList(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		List<OnSiteMarketingLocation> locations = service.findAll(organization, property);
		List<LocationDto> dtoLocations = locations.stream().map(LocationDto::new).collect(Collectors.toList());

		List<Lookup> locationTypes = lookupService.getLookUpData(organization, LookupService.LOCATION_TYPE, property);
		List<LookUpDto> dtoTypes = locationTypes.stream().map(LookUpDto::new).collect(Collectors.toList());

		return new LocationDataDto(dtoLocations, dtoTypes);
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping("/location/batch")
	public List<LocationDto> update(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @RequestBody List<LocationDto> entities) {

		List<OnSiteMarketingLocation> locationUpdates = entities.stream().map(OnSiteMarketingLocation::new)
				.collect(Collectors.toList());

		List<OnSiteMarketingLocation> locations = this.service.findAll(organization, property);
		List<Lookup> locationTypesRaw = lookupService.getLookUpData(organization, LookupService.LOCATION_TYPE,
				property);
		Set<String> locationTypes = locationTypesRaw.stream().map(lT -> lT.getId().getLookUpCode().toString())
				.collect(Collectors.toSet());

		locationUpdates.forEach((loc) -> {
			if (locations.contains(loc)) {
				if (!locationTypes.contains(loc.getLocationTypeCode())) {
					throw new InvalidClientRequest(); // Location type does not
														// exist
				}
				locations.set(locations.indexOf(loc), loc);
			} else {
				throw new InvalidClientRequest(); // Cant find original to
													// update
			}
		});

		this.service.save(locationUpdates);
		return locations.stream().map(LocationDto::new).collect(Collectors.toList());
	}

}
