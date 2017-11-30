package com.vistana.onsiteconcierge.rest;

import com.vistana.onsiteconcierge.core.dto.PropertyDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.LeadContactDto;
import com.vistana.onsiteconcierge.core.dto.LookUpDto;
import com.vistana.onsiteconcierge.core.dto.LookupDataDto;
import com.vistana.onsiteconcierge.core.exception.ResourceNotFoundException;
import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.model.Lookup;
import com.vistana.onsiteconcierge.core.model.Property;
import com.vistana.onsiteconcierge.core.service.LeadContactService;
import com.vistana.onsiteconcierge.core.service.LookupCategoryService;
import com.vistana.onsiteconcierge.core.service.LookupService;
import com.vistana.onsiteconcierge.core.service.PropertyService;

@RestController
public class LookUpRest {

	@Autowired
	private LeadContactService leadContactService;

	@Autowired
	private LookupCategoryService lookUpCategoryService;

	@Autowired
	private LookupService lookUpService;

	@Autowired
	private PropertyService propertyService;

	/**
	 * Return customer information for a given customer id.
	 *
	 * @param organization
	 *            - Organization
	 * @param property
	 *            - Property
	 * @param customerId
	 *            - Customer Id
	 * @return LeadContectDto
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/lookup/customer/{customerId}")
	public LeadContactDto getCustomerInfo(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @PathVariable Integer customerId) {

		LeadContact leadContact = leadContactService.getLeadContacts(organization, property, customerId);
		return new LeadContactDto(leadContact);
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping(value = "/leadDetails/trackingComments")
	public LookupDataDto getLeadDetailLookupData(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		String[] lookupCategories = { LookupService.GUEST_TYPE, LookupService.ACTIVATOR_TYPE,
				LookupService.CONTACT_TYPE, LookupService.GIFT_TYPE, LookupService.GIFT_STATUS,
				LookupService.GUEST_LEVEL };

		Map<String, List<Lookup>> returnList = lookUpService.getLookUpData(organization, property, lookupCategories);
		LookupDataDto lookupDataDto = new LookupDataDto();
		lookupDataDto.setGuestTypes(
				returnList.get(LookupService.GUEST_TYPE).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setActivatorTypes(
				returnList.get(LookupService.ACTIVATOR_TYPE).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setContactTypes(
				returnList.get(LookupService.CONTACT_TYPE).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setGiftTypes(
				returnList.get(LookupService.GIFT_TYPE).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setGiftStatus(
				returnList.get(LookupService.GIFT_STATUS).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setSpgLevels(
				returnList.get(LookupService.GUEST_LEVEL).stream().map(LookUpDto::new).collect(Collectors.toList()));
		return lookupDataDto;
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/lookup/category/{category}")
	public List<LookUpDto> getLookUpDataByCategory(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @PathVariable String category) {

		if (!lookUpCategoryService.doesCategoryExist(organization, category)) {
			throw new ResourceNotFoundException();
		}
		List<Lookup> lookUps = lookUpService.getLookUpData(organization, category, property);
		return lookUps.stream().map(LookUpDto::new).collect(Collectors.toList());
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/lookup/properties")
	public Map<Integer, PropertyDto> getPropertyData(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		Set<PropertyDto> properties = propertyService.findAllByIdOrganizationId(organization).stream()
				.filter(p -> p.getPropertyId() > 0).collect(Collectors.toSet());

		HashMap<Integer, PropertyDto> props = new HashMap<>();

		properties.forEach((p) -> props.put(p.getPropertyId(), p));

		return props;
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/lookup/lookupdesc")
	public List<String> getLookUpDesc(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		return lookUpService.getLookUpDescList(organization, property);
	}

}
