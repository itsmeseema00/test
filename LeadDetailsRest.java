package com.vistana.onsiteconcierge.rest;

import static com.vistana.onsiteconcierge.config.Constants.LOOKUP_GUEST_TYPE;
import static com.vistana.onsiteconcierge.config.Constants.LOOKUP_HOTEL;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.*;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.exception.SecurityAccessException;
import com.vistana.onsiteconcierge.core.model.*;
import com.vistana.onsiteconcierge.core.service.*;

@RestController
public class LeadDetailsRest {
	@Autowired
	protected LeadContactHistoryService historyService;

	@Autowired
	protected LeadContactService leadContactService;

	@Autowired
	protected LeadGiftService leadGiftService;

	@Autowired
	protected LookupService lookupService;

	@Autowired
	protected TourService tourService;

	/**
	 * Batch update lead detail comments
	 *
	 * @param organization
	 *            The organization id
	 * @param property
	 *            THe prop id
	 * @param newLch
	 *            Lead Contact History body
	 * @return The updated history
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping("/leadDetails/comments")
	public LeadContactHistory batchUpdate(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) int property, @RequestBody LeadContactHistoryDto newLch) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		AuthenticatedUserDto user = AuthenticatedUserDto.getUser(authentication.getPrincipal());
		List<String> accesses = user.getProperty(organization, property).getAccesses();

		if (newLch != null) {
			LeadContact existingLead = leadContactService.findOneLead(newLch.getOrganizationId(),
					newLch.getPropertyId(), newLch.getReservationConfirmationNum(), newLch.getRoomSequence(),
					newLch.getGuestSequence());
			if (existingLead.getInvitedFlag()) {
				newLch.setInvitedFlag(true);
			}
			if (existingLead.getContactFlag()) {
				newLch.setContactFlag(true);
			}
			if (newLch.getContactFlag() == null) {
				newLch.setContactFlag(false);
			}
			if (newLch.getInvitedFlag() == null) {
				newLch.setInvitedFlag(false);
			}
			if (newLch.getShowOnTripTicketFlag() == null) {
				newLch.setShowOnTripTicketFlag(false);
			}
			if (newLch.getPromiseGiftFlag() == null) {
				newLch.setPromiseGiftFlag(false);
			}
			if (newLch.getIssuedFlag() == null) {
				newLch.setIssuedFlag(false);
			}
			if (newLch.getOverrideLeadStatusFlag() == null) {
				newLch.setOverrideLeadStatusFlag(false);
			}

			LeadContact update = new LeadContact(newLch);
			update.setArrivalDate(existingLead.getArrivalDate());
			update.setDepartureDate(existingLead.getDepartureDate());

			if (existingLead.getInitialContactedPersonId() == null && newLch.getContactFlag()) {
				update.setInitialContactedPersonId(user.getId());
			} else if (!Objects.equals(existingLead.getInitialContactedPersonId(), newLch.getInitialContactedPersonId())
					&& !accesses.contains(CoreConstants.ACCESS_CHANGE_INITIAL_CONTACT)) {
				throw new SecurityAccessException();
			}

			if (accesses.contains(CoreConstants.ACCESS_REASSIGN_VSC)) {
                update.setAllocatedPersonId(newLch.getAllocatedPersonId());

            } else {
//			    if(existingLead.getLeadStatusCode()==null) {
                    leadContactService.updateLeadContact(update, update.getLeadStatusCode(), user.getId());
//                }
			}

			if (existingLead.getAllocatedPersonId() == null && existingLead.getInitialAllocatedPersonId() == null
					&& update.getAllocatedPersonId() != null) {
				update.setInitialAllocatedPersonId(update.getAllocatedPersonId());
			} else {
				update.setInitialAllocatedPersonId(existingLead.getInitialAllocatedPersonId());
			}
			leadContactService.save(update);

			if (newLch.getPromiseGiftFlag() || newLch.getIssueGiftFlag()) {
				List<LeadGift> leadGifts = leadGiftService.findLeadGift(organization, property,
						newLch.getReservationConfirmationNum());
				boolean isAGiftIssue = newLch.getGiftStatusCode().equals("IS");
				// Update Path
				if (newLch.getGiftSequence() != null) {
					LeadGift giftToUpdate = leadGifts.get(leadGifts.indexOf(new LeadGift(newLch)));
					if (giftToUpdate == null) {
						throw new InvalidClientRequest("Gift does not exist");
					}

					boolean currentlyUnissued = !giftToUpdate.getGiftStatusCode().equals("IS")
							&& (giftToUpdate.getIssueDtm() == null && giftToUpdate.getIssuePersonId() == null);

					giftToUpdate.setGiftStatusCode(newLch.getGiftStatusCode());
					giftToUpdate.setGiftSerialNum(newLch.getSerialNumber());
					giftToUpdate.setGiftId(newLch.getGiftId());
					giftToUpdate.setActivatorId(newLch.getActivatorId());

					if (isAGiftIssue && currentlyUnissued) {
						giftToUpdate.setIssueDtm(newLch.getCreateDtm());
						giftToUpdate.setIssuePersonId(newLch.getUpdatePersonId());
					}

					if (!isAGiftIssue && !currentlyUnissued) {
						giftToUpdate.setIssueDtm(null);
						giftToUpdate.setIssuePersonId(null);
						giftToUpdate.setGiftSerialNum(null);
					}
					//
					//

					leadGiftService.save(giftToUpdate);
					// New Gift Path
				} else {
					newLch.setGiftSequence(leadGifts.size() + 1);
					LeadGift gift = new LeadGift(newLch);
					if (!isAGiftIssue) {
						gift.setIssueDtm(null);
						gift.setIssuePersonId(null);
					} else {
						newLch.setIssueGiftFlag(true);
						gift = new LeadGift(newLch);
					}
					leadGiftService.save(gift);
				}
			}

			String[] lookupCategories = { LookupService.CONTACT_TYPE, LOOKUP_GUEST_TYPE, LOOKUP_HOTEL,
					LookupService.OWNER_PROPERTY };
			Map<String, List<Lookup>> lookups = lookupService.getLookUpData(organization, property, lookupCategories);

			List<Lookup> contactTypes = lookups.get(LookupService.CONTACT_TYPE);
			update.setContactType(contactTypes);
			List<Lookup> guestTypes = lookups.get(LOOKUP_GUEST_TYPE);
			update.setGuestType(guestTypes);

			return new LeadContactHistory(update);
		} else {
			return null;
		}
	}

	/**
	 * Return lead detail history
	 *
	 * @param resNumber
	 *            The guests reservation number
	 * @param roomSeq
	 *            The guests room sequence
	 * @param guestSeq
	 *            The guests sequence number
	 * @param organization
	 *            The organization id
	 * @param property
	 *            The current property
	 * @return List of lead contact histories.
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/leadDetails/history/{resNumber}/{roomSeq}/{guestSeq}")
	public List<LeadContactHistoryDto> getLeadDetailHistory(@PathVariable String resNumber,
			@PathVariable Integer roomSeq, @PathVariable Integer guestSeq,
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		Map<String, List<Lookup>> lookUpData = lookupService.getLookUpData(organization, property,
				new String[] { LookupService.CONTACT_TYPE, LOOKUP_GUEST_TYPE });
		List<LeadContactHistory> leadContactHistory = historyService
				.findAllComments(new GuestId(organization, property, resNumber, roomSeq, guestSeq));
		leadContactHistory.sort((lch1, lch2) -> Long.compare(lch2.getId().getCreateDtm().getTime(),
				lch1.getId().getCreateDtm().getTime()));
		return leadContactHistory.stream().map((history) -> {
			history.setContactType(lookUpData.get(LookupService.CONTACT_TYPE));
			history.setGuestType(lookUpData.get(LOOKUP_GUEST_TYPE));
			return new LeadContactHistoryDto(history);
		}).collect(Collectors.toList());
	}

	/**
	 * Retrieve lead detail data
	 *
	 * @param resNumber
	 *            The leads reservation number
	 * @param roomSeq
	 *            The leads room sequence
	 * @param guestSeq
	 *            The leads guest sequence
	 * @param customerUniqueId
	 *            The leads customer unique id
	 * @param organization
	 *            The leads organization id
	 * @param property
	 *            The leads property
	 * @return Leads detail record corresponding to the guest given.
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/leadDetails/{resNumber}/{roomSeq}/{guestSeq}/{customerUniqueId}")
	public LeadDetailDto getLeadDetailsData(@PathVariable String resNumber, @PathVariable Integer roomSeq,
			@PathVariable Integer guestSeq, @PathVariable Integer customerUniqueId,
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		int index = 1;
		LeadContact lead = leadContactService
				.findFullyLoadedWithoutHistories(new GuestId(organization, property, resNumber, roomSeq, guestSeq));
		if (lead == null) {
			return new LeadDetailDto();
		}

		LeadContactHistory updated = new LeadContactHistory(lead);
		updated.setRemarksText("Viewed", true);
		historyService.save(updated);

		String[] lookupCategories = { LookupService.CONTACT_TYPE, LOOKUP_GUEST_TYPE, LOOKUP_HOTEL,
				LookupService.OWNER_PROPERTY };
		Map<String, List<Lookup>> lookups = lookupService.getLookUpData(organization, property, lookupCategories);

		List<Lookup> contactTypes = lookups.get(LookupService.CONTACT_TYPE);
		lead.setContactType(contactTypes);

		List<Lookup> guestTypes = lookups.get(LOOKUP_GUEST_TYPE);
		lead.setGuestType(guestTypes);

		lead.getOwners().forEach(one -> one.getOwnerContracts().forEach(two -> {
			List<Lookup> ownerProperties = lookups.get(LookupService.OWNER_PROPERTY);
			two.setPropertyName(ownerProperties);

		}));

		if (lead.getPackages() != null) {
			List<Lookup> hotels = lookups.get(LOOKUP_HOTEL);
			lead.getPackages().forEach(one -> one.setHotelName(hotels));
		}
		LeadDetailDto dto = new LeadDetailDto(lead);
		Set<String> resNumbers = new HashSet<>();
		Set<Integer> customerUniqueIds = new HashSet<>();
		List<LeadContact> leadContacts = new ArrayList<>();
		List<LeadContact> finalLeads = new ArrayList<>();
		if (customerUniqueId != null) {
			leadContacts
					.addAll(leadContactService.findByResNumOrCUI(organization, property, resNumber, customerUniqueId));
		}
		leadContacts.forEach(one -> {
			resNumbers.add(one.getId().getReservationConfirmationNum());
			customerUniqueIds.add(one.getCustomerUniqueId());

		});
		if (customerUniqueIds.size() > 1) {
			final List<LeadContact> lds = new ArrayList<>();
			customerUniqueIds.forEach(three -> {
				if (three != null) {
					lds.addAll(leadContactService.findByCustomerUniqueId(organization, property, three));
				}
			});
			finalLeads.addAll(lds);
		} else {
			final List<LeadContact> lds = new ArrayList<>();
			resNumbers.forEach(two -> lds.addAll(leadContactService.findByResNum(organization, property, two)));
			finalLeads.addAll(lds);
		}
		finalLeads = finalLeads.stream()
				.filter((ld) -> ld.getGuestStatus() != null && !ld.getGuestStatus().equals("CXL"))
				.collect(Collectors.toList());

		finalLeads.sort((one, two) -> {

			if (one.getArrivalDate() == null || two.getArrivalDate() == null) {
				return 0;
			}
			return one.getArrivalDate().compareTo(two.getArrivalDate());
		});

		// check if one lead has multiple reservations
		if (finalLeads.size() > 1) {
			for (LeadContact each : finalLeads) {
				dto.getDropdownValues().add(new LeadDetailDropdownDto(each, index, false));
				if (each.getId().getReservationConfirmationNum().equals(resNumber)
						&& each.getId().getRoomSequence().equals(roomSeq)
						&& each.getId().getGuestSequence().equals(guestSeq)) {
					dto.setIndex(index);
				}
				index++;
			}
		}

		return dto;
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/leadDetails/gifts/{reservationConfNum}")
	public List<LeadGiftDto> getLeadGifts(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) int property, @PathVariable String reservationConfNum) {

		List<LeadGift> leadGifts = leadGiftService.findLeadGift(organization, property, reservationConfNum);
		return leadGifts.isEmpty() ? new ArrayList<>()
				: leadGifts.stream().map(LeadGiftDto::new).collect(Collectors.toList());

	}

	/**
	 * Get reservation comments for a customer
	 *
	 * @param organization
	 *            - The current logged in organization
	 * @param property
	 *            - The currently logged in property
	 * @param customerUniqueId
	 *            - The customers unique identification number
	 * @return List of Lead contact histories.
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/otherResComments/{customerUniqueId}")
	public List<LeadContactHistoryDto> getOtherResComments(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @PathVariable Integer customerUniqueId) {

		List<LeadContactHistoryDto> results = new ArrayList<>();
		List<LeadContactHistory> found;

		found = historyService.findAllComments(organization, property, customerUniqueId);
		String[] lookupCategories = { LookupService.CONTACT_TYPE, LOOKUP_GUEST_TYPE, LOOKUP_HOTEL };
		Map<String, List<Lookup>> lookups = lookupService.getLookUpData(organization, property, lookupCategories);
		List<Lookup> contactTypes = lookups.get(LookupService.CONTACT_TYPE);
		List<Lookup> guestTypes = lookups.get(LOOKUP_GUEST_TYPE);
		for (LeadContactHistory lead : found) {
			lead.setContactType(contactTypes);
			lead.setGuestType(guestTypes);
		}

		return results;

	}

	/**
	 * Lik a tour to a guest based on available information
	 *
	 * @param organization
	 *            The current logged in organization
	 * @param property
	 *            The currently logged in property
	 * @param resNumber
	 *            The reservation number used to locate the guest information
	 * @param roomSeq
	 *            The room sequence of the guest
	 * @param guestSeq
	 *            Guest sequence
	 * @param customerUniqueId
	 *            The id of the customer.
	 * @param entity
	 *            Link DTO which contains the nescisary
	 * @return The tour that was just linked.
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping("/leadDetails/{resNumber}/{roomSeq}/{guestSeq}/{customerUniqueId}")
	public Tour linkTour(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @PathVariable String resNumber,
			@PathVariable Integer roomSeq, @PathVariable Integer guestSeq, @PathVariable Integer customerUniqueId,
			@RequestBody LinkDto entity) {

		LeadContact lead = leadContactService.find(new GuestId(organization, property, resNumber, roomSeq, guestSeq));
		if (lead == null) {
			throw new InvalidClientRequest("Could not find associated lead");
		}
		if (entity.getTripTicketNumber() == null) {
			throw new InvalidClientRequest("Trip Ticket number is required to link tours");

		}
		TourId tourId = new TourId(organization, entity.getInternalPropertyId(), entity.getTripTicketNumber());
		return tourService.linkGuest(tourId, lead);

	}

}
