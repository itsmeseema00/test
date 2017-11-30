package com.vistana.onsiteconcierge.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.config.service.CallCenterService;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.CallCenterCommentDto;
import com.vistana.onsiteconcierge.core.service.PropertyService;
import com.vistana.webservices.SunServiceException;

/**
 * Handles call center actions.
 */
@RestController
@RequestMapping("/callCenter")
public class CallCenterRest {
	private static final Logger log = LoggerFactory.getLogger(CallCenterRest.class);

	@Autowired
	private CallCenterService callCenterService;
	@Autowired
	private PropertyService propertyService;

	/**
	 * Get call center comments by owner
	 * 
	 * @param organization
	 *            The organization
	 * @param property
	 *            The four digit property id
	 * @param ownerNumber
	 *            The owner number
	 * @return List of call center comments
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/comments/owner/{ownerNumber}")
	public List<CallCenterCommentDto> getCommentsByOwner(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @PathVariable String ownerNumber) {

		try {
			return callCenterService.getCommentsByOwner(ownerNumber).stream().map(CallCenterCommentDto::new)
					.collect(Collectors.toList());
		} catch (SunServiceException e) {
			log.error("Could not retrieve call center comments for owner: " + ownerNumber, e);
			return new ArrayList<>();
		}
	}

	/**
	 * Get call center comments by trip ticket number
	 * 
	 * @param organization
	 *            The organization id
	 * @param property
	 *            The four digit property id
	 * @param ticketNumber
	 *            The trip ticket number
	 * @param propertyNumber
	 *            The four digit property id for the trip ticket.
	 * @return List of call center comments
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/comments/ticket/{propertyNumber}/{ticketNumber}")
	public List<CallCenterCommentDto> getCommentsByTicket(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @PathVariable String ticketNumber,
			@PathVariable String propertyNumber) {

		try {

			return callCenterService.getCommentsByTripTicket(ticketNumber, propertyNumber).stream()
					.map(CallCenterCommentDto::new).collect(Collectors.toList());
		} catch (SunServiceException e) {
			log.error("Could not retrieve call center comments for trip ticket: " + ticketNumber, e);
			return new ArrayList<>();
		}
	}

	/**
	 * Get call center comments by accomodation/reservation number
	 * 
	 * @param organization
	 *            The organization id
	 * @param property
	 *            The property id
	 * @param accommodationNumber
	 *            The accommodation number, also known as the reservation number
	 * @return List of call center comments
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/comments/accom/{accommodationNumber}")
	public List<CallCenterCommentDto> getCommentsByAccommodation(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @PathVariable String accommodationNumber) {

		try {
			return callCenterService.getCommentsByAccomConfNum(String.valueOf(property), accommodationNumber).stream()
					.map((each) -> {
						CallCenterCommentDto dto = new CallCenterCommentDto(each);
						dto.setDate(propertyService.getTZCorrectedDate(dto.getDate(), property, organization));
						return dto;
					}).collect(Collectors.toList());
		} catch (SunServiceException e) {
			log.error("Could not retrieve call center with accommodation number: " + accommodationNumber, e);
			return new ArrayList<>();
		}

	}

}
