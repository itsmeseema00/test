package com.vistana.onsiteconcierge.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import com.vistana.onsiteconcierge.core.dto.GiftDto;
import com.vistana.onsiteconcierge.core.exception.GenericException;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.model.Gift;
import com.vistana.onsiteconcierge.core.model.IRankable;
import com.vistana.onsiteconcierge.core.service.GiftService;
import com.vistana.onsiteconcierge.core.service.OrganizationService;
import com.vistana.onsiteconcierge.core.service.PropertyService;

@RestController
public class GiftRest {

	private static final Boolean ACTIVE_FLAG = true;

	@Autowired
	protected GiftService giftService;

	@Autowired
	protected OrganizationService organizationService;

	@Autowired
	protected PropertyService propertyService;

	/**
	 * Adds a new Activator Gift
	 *
	 * @param organization
	 *            - Organization
	 * @param property
	 *            - Property
	 * @param dto
	 *            - Dto
	 * @return {@link Set<GiftDto>}
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ADMINISTRATOR)
	@PostMapping("gift")
	public List<GiftDto> addGift(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @Valid @RequestBody GiftDto dto,
			BindingResult result) {

		if (result.hasErrors()) {
			for (FieldError error : result.getFieldErrors()) {
				String err = "error: {} in field :" + error.getField() + ", " + error.getObjectName() + " "
						+ error.getDefaultMessage();
				throw new InvalidClientRequest("Exception Caught: " + err);
			}
		}
		Gift create = new Gift(dto);

		List<Gift> gifts = giftService.findByOrganizationIdAndPropertyId(organization, property);

		create.setOrganization(organization);
		create.setProperty(property);

		if (!create.getOrganizationId().equals(organization) || !create.getPropertyId().equals(property)) {
			throw new InvalidClientRequest();
		} else if (gifts.contains(create)) {
			throw new InvalidClientRequest();
		}

		if (!create.getActiveFlag()) {
			create.setSortOrder(gifts.size() + 1);
		}

		List<Gift> updateList = IRankable.add(gifts, create);

		giftService.save(updateList);
		return this.getAllActivatorGifts(organization, property);
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/giftsAll")
	public List<GiftDto> getAllActivatorGifts(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		List<Gift> gifts = giftService.findByOrganizationIdAndPropertyId(organization, property);
		return gifts.stream().map(GiftDto::new).collect(Collectors.toList());
	}

	/**
	 * Retrieves all Gifts by Organization and Property
	 *
	 * @param organization
	 *            - Organization
	 * @param property
	 *            - Property
	 * @return {@link Set<GiftDto>}
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/giftsActive")
	public List<GiftDto> getAllActiveActivatorGifts(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		List<Gift> gifts = giftService.findByOrganizationIdAndPropertyIdAndActiveFlag(organization, property,
				ACTIVE_FLAG);
		return gifts.stream().map(GiftDto::new).collect(Collectors.toList());
	}

	/**
	 * Saves data that has been modified on the Activator Gift page
	 *
	 * @param organization
	 *            - Organization
	 * @param property
	 *            - Property
	 * @param dtos
	 *            = Dto
	 * @return {@link GiftDto}
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ADMINISTRATOR)
	@PostMapping("gifts")
	public List<GiftDto> updateAllGifts(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @Valid @RequestBody Set<GiftDto> dtos,
			BindingResult result) {

		if (result.hasErrors()) {
			for (FieldError error : result.getFieldErrors()) {
				String err = "error: {} in field :" + error.getField() + ", " + error.getObjectName() + " "
						+ error.getDefaultMessage();
				throw new InvalidClientRequest("Exception Caught: " + err);
			}
		}

		try {
			List<Gift> saved = new ArrayList<>(giftService.findByOrganizationIdAndPropertyId(organization, property));

			List<Gift> updates = dtos.stream().filter(Objects::nonNull).map(Gift::new).collect(Collectors.toList());

			int sortOrderCorrection = 0;

			for (Gift entity : updates) {
				entity.setOrganization(organization);
				entity.setProperty(property);

				if (!saved.contains(entity)) {
					throw new InvalidClientRequest();
				}

				Gift old = saved.get(saved.indexOf(entity));

				if (entity.getSortOrder() == null || entity.getSortOrder() > saved.size()) {
					entity.setSortOrder(saved.size() - sortOrderCorrection);
				} else if (old.getActiveFlag() && !entity.getActiveFlag()) {
					entity.setSortOrder(saved.size() - sortOrderCorrection);
					sortOrderCorrection++;
				}
			}

			Set<Gift> sorted = IRankable.set(saved, updates);

			giftService.save(sorted);
		} catch (Exception e) {
			throw new GenericException("Exception Caught: " + e.getMessage());
		}

		return this.getAllActivatorGifts(organization, property);
	}

}
