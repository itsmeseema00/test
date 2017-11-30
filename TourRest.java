package com.vistana.onsiteconcierge.rest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.config.service.TourBookService;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.AuthenticatedLoginDto;
import com.vistana.onsiteconcierge.core.dto.AuthenticatedOrganizationDto;
import com.vistana.onsiteconcierge.core.dto.AuthenticatedPropertyDto;
import com.vistana.onsiteconcierge.core.dto.AuthenticatedUserDto;
import com.vistana.onsiteconcierge.core.dto.LeadSourceDto;
import com.vistana.onsiteconcierge.core.dto.LookUpDto;
import com.vistana.onsiteconcierge.core.dto.LookupDataDto;
import com.vistana.onsiteconcierge.core.dto.PersonPropertyDto;
import com.vistana.onsiteconcierge.core.dto.PropertyDto;
import com.vistana.onsiteconcierge.core.dto.TourDto;
import com.vistana.onsiteconcierge.core.dto.TourEmailDto;
import com.vistana.onsiteconcierge.core.dto.TourManifestDto;
import com.vistana.onsiteconcierge.core.dto.TourSearchDto;
import com.vistana.onsiteconcierge.core.dto.TourSearchResultDto;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.model.Guest;
import com.vistana.onsiteconcierge.core.model.GuestId;
import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.model.LeadSource;
import com.vistana.onsiteconcierge.core.model.LeadSourceId;
import com.vistana.onsiteconcierge.core.model.Lookup;
import com.vistana.onsiteconcierge.core.model.LookupId;
import com.vistana.onsiteconcierge.core.model.Owner;
import com.vistana.onsiteconcierge.core.model.Package;
import com.vistana.onsiteconcierge.core.model.Person;
import com.vistana.onsiteconcierge.core.model.Property;
import com.vistana.onsiteconcierge.core.model.PropertyId;
import com.vistana.onsiteconcierge.core.model.Rate;
import com.vistana.onsiteconcierge.core.model.RateId;
import com.vistana.onsiteconcierge.core.model.Tour;
import com.vistana.onsiteconcierge.core.model.TourEmail;
import com.vistana.onsiteconcierge.core.model.TourEmailId;
import com.vistana.onsiteconcierge.core.model.TourId;
import com.vistana.onsiteconcierge.core.service.LeadContactService;
import com.vistana.onsiteconcierge.core.service.LeadSourceService;
import com.vistana.onsiteconcierge.core.service.LookupService;
import com.vistana.onsiteconcierge.core.service.PersonService;
import com.vistana.onsiteconcierge.core.service.PropertyService;
import com.vistana.onsiteconcierge.core.service.RateService;
import com.vistana.onsiteconcierge.core.service.TourEmailService;
import com.vistana.onsiteconcierge.core.service.TourSearchService;
import com.vistana.webservices.SunServiceException;
import com.vistana.webservices.internal.model.TripTicket;
import com.vistana.webservices.internal.model.TripTicketList;
import com.vistana.webservices.internal.model.TripTicketManifestList;

@RestController
@RequestMapping("/tour")
public class TourRest {

	private static Logger log = LoggerFactory.getLogger(TourRest.class);

	public static final String DG_USER = "DGUSER";
	@Autowired
	private ObjectMapper jsonMapper;
	@Autowired
	private LeadContactService leadContactService;

	@Autowired
	private LeadSourceService leadSourceService;

	@Autowired
	private LookupService lookUpService;

	@Autowired
	private PersonService personService;

	@Autowired
	private PropertyService propertyService;

	@Autowired
	private RateService rateService;

	@Autowired
	private TourBookService tourBookService;

	@Autowired
	private TourEmailService tourEmailService;

	@Autowired
	private TourSearchService tourSearchService;

	/**
	 * Cancels a tour, tour will be marked cancelled, rather than be removed.
	 *
	 * @param organization
	 *            - The organization
	 * @param property
	 *            - The property
	 * @param dto
	 *            - The tour dto, which also will contain the original trip ticket encoded as a string.
	 * @return new tour with updated status
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping(value = "/cancel")
	public TourDto cancelTour(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @RequestBody TourDto dto) {

		AuthenticatedUserDto user = AuthenticatedUserDto
				.getUser(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		TourDto cancelledTour = tourBookService.cancelTour(dto, organization, property, getSunLogin(user));
		cancelledTour.setProperty(new PropertyDto(propertyService.findById(new PropertyId(organization, property))));
		return cancelledTour;
	}

	/**
	 * Creates a new Trip Ticket
	 *
	 * @param organization
	 *            - Org id
	 * @param property
	 *            - Property code
	 * @param dto
	 *            - Tour data
	 * @return {@link TourDto}
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping
	public TourDto createNewTicket(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @RequestBody TourDto dto) {

		AuthenticatedUserDto user = AuthenticatedUserDto
				.getUser(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		TourDto createdTour = tourBookService.createTour(dto, organization, property, getSunLogin(user));
		createdTour.setProperty(new PropertyDto(propertyService.findById(new PropertyId(organization, property))));
		return createdTour;
	}

	/**
	 * Returns the address data for a given property and organization
	 *
	 * @param organization
	 *            - Org id
	 * @param property
	 *            - Property id
	 * @return List of lookup data.
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping(value = "/address")
	public List<LookUpDto> getAddressData(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		List<Lookup> stateCountries = lookUpService.getLookUpData(organization, LookupService.STATECOUNTRY, property);
		return stateCountries.stream().map(LookUpDto::new).collect(Collectors.toList());
	}

	/**
	 * Gets all Drop Down values for the Tour Booking Form
	 *
	 * @param organization
	 *            - Org code
	 * @param property
	 *            - Property id
	 * @return {@link LookupDataDto}
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping(value = "/general")
	public LookupDataDto getGeneralTab(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		String[] lookupCategories = { LookupService.MARKETING_SOURCE, LookupService.TOUR_LOCATION, LookupService.HOTEL,
				LookupService.STATECOUNTRY, LookupService.SALESCENTER, LookupService.SALESLINE,
				LookupService.DG_GIFT_TYPE, LookupService.GIFT_OCCURRENCE, LookupService.EMAIL_TYPE,
				LookupService.EMAIL_SOURCE, LookupService.DEACTIVE_SOURCE, LookupService.NO_PAY,
				LookupService.CREDIT_CARD, LookupService.TOUR_CODE, LookupService.TOUR_TYPE, LookupService.SOLICITOR,
				LookupService.TOUR_FILTER, LookupService.MANIFEST, LookupService.TOUR_GIFT, LookupService.SAMPLER_PITCH,
				LookupService.OWNER_PROPERTY };

		Map<String, List<Lookup>> returnList = lookUpService.getLookUpData(organization, property, lookupCategories);
		LookupDataDto lookupDataDto = new LookupDataDto();
		lookupDataDto.setTourTypes(
				returnList.get(LookupService.TOUR_TYPE).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setMarketingSources(returnList.get(LookupService.MARKETING_SOURCE).stream().map(LookUpDto::new)
				.collect(Collectors.toList()));
		lookupDataDto.setTourLocations(
				returnList.get(LookupService.TOUR_LOCATION).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setHotels(
				returnList.get(LookupService.HOTEL).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setSalesCenters(
				returnList.get(LookupService.SALESCENTER).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setSalesLines(
				returnList.get(LookupService.SALESLINE).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setGiftTypes(
				returnList.get(LookupService.DG_GIFT_TYPE).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setGiftOccurrences(returnList.get(LookupService.GIFT_OCCURRENCE).stream().map(LookUpDto::new)
				.collect(Collectors.toList()));
		lookupDataDto.setEmailTypes(
				returnList.get(LookupService.EMAIL_TYPE).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setEmailSources(
				returnList.get(LookupService.EMAIL_SOURCE).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setDeactiveSources(returnList.get(LookupService.DEACTIVE_SOURCE).stream().map(LookUpDto::new)
				.collect(Collectors.toList()));
		lookupDataDto.setCreditCardCodes(
				returnList.get(LookupService.CREDIT_CARD).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setTourCodes(
				returnList.get(LookupService.TOUR_CODE).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setTourFilters(
				returnList.get(LookupService.TOUR_FILTER).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setManifests(
				returnList.get(LookupService.MANIFEST).stream().map(LookUpDto::new).collect(Collectors.toList()));
		List<Person> persons = personService.findByOrganizationAndPropertyAndActive(organization, property, true);
		lookupDataDto.setPersons(persons.stream().map(person -> new PersonPropertyDto(organization, property, person))
				.collect(Collectors.toList()));
		lookupDataDto.setSolicitors(
				returnList.get(LookupService.SOLICITOR).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setLeadSources(leadSourceService.findAll(organization, property).stream().map(LeadSourceDto::new)
				.collect(Collectors.toList()));
		lookupDataDto.setTourFilters(
				returnList.get(LookupService.TOUR_FILTER).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setTourGifts(
				returnList.get(LookupService.TOUR_GIFT).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setNoPayReasons(
				returnList.get(LookupService.NO_PAY).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setLeaseSamplerPitchedInds(
				returnList.get(LookupService.SAMPLER_PITCH).stream().map(LookUpDto::new).collect(Collectors.toList()));
		lookupDataDto.setOwnerProperties(
				returnList.get(LookupService.OWNER_PROPERTY).stream().map(LookUpDto::new).collect(Collectors.toList()));
		List<LookUpDto> stateCountry = returnList.get(LookupService.STATECOUNTRY).stream().map(LookUpDto::new)
				.collect(Collectors.toList());
		lookupDataDto.setStateCountries(stateCountry);
		lookupDataDto.setStates(sortFilterStates(stateCountry));
		lookupDataDto.setCountries(sortFilterCountries(stateCountry));
		return lookupDataDto;

	}

	/**
	 * Simply matches a lookup code with its description
	 *
	 * @return -
	 */
	private String getManifestDesc(String organization, Integer propertyCode, String manifestCode) {

		if (manifestCode != null && !manifestCode.isEmpty()) {
			Lookup lookup = lookUpService
					.find(new LookupId(organization, propertyCode, LookupService.MANIFEST, manifestCode));
			return lookup != null ? lookup.getLookupDesc() : "";
		} else {
			return null;
		}
	}

	private TourDto applyCountryFix(TourDto tourDto, Guest guest) {

		if (guest != null && guest.getCountry() != null) {
			if (guest.getCountry().equalsIgnoreCase("CA")) {
				tourDto.getAddress().setStateCountryCd("CAN");
				tourDto.getAddress().setProvince(guest.getState());
				tourDto.getAddress().setCity(guest.getCity());
				tourDto.setCountryCd("CAN");
			} else if (guest.getCountry().equalsIgnoreCase("GB")) {
				tourDto.getAddress().setStateCountryCd("UNK");
				tourDto.getAddress().setProvince(guest.getState());
				tourDto.getAddress().setCity(guest.getCity());
				tourDto.setCountryCd("UNK");
			} else if (guest.getCountry().equalsIgnoreCase("JP") || guest.getCountry().equalsIgnoreCase("JPN")) {
				tourDto.getAddress().setStateCountryCd("JAP");
				tourDto.getAddress().setProvince(guest.getState());
				tourDto.getAddress().setCity(guest.getCity());
				tourDto.setCountryCd("JAP");
			} else if (guest.getCountry().equalsIgnoreCase("MEXI") || guest.getCountry().equalsIgnoreCase("MX")) {
				tourDto.getAddress().setStateCountryCd("MEX");
				tourDto.getAddress().setProvince(guest.getState());
				tourDto.getAddress().setCity(guest.getCity());
				tourDto.setCountryCd("MEX");
			} else if (guest.getCountry().equalsIgnoreCase("AU")) {
				tourDto.getAddress().setStateCountryCd("AUS");
				tourDto.getAddress().setProvince(guest.getState());
				tourDto.getAddress().setCity(guest.getCity());
				tourDto.setCountryCd("AUS");
			}
		}

		return tourDto;
	}

	/**
	 * Returns a trip ticket that has been populated with guest information.
	 *
	 * @param organization
	 *            - The org id
	 * @param property
	 *            - The Marriott recognised property id.
	 * @param roomSequence
	 *            - Room sequence number
	 * @param guestSequence
	 *            - Guest sequence number
	 * @param resNumber
	 *            - The reservation number
	 * @return TourDto populated with information from the guest.
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/customer/{resNumber}/{roomSequence}/{guestSequence}")
	public TourDto getPopulatedTicket(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @PathVariable Integer roomSequence,
			@PathVariable Integer guestSequence, @PathVariable String resNumber) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		AuthenticatedUserDto authenticatedUserDto = AuthenticatedUserDto.getUser(authentication.getPrincipal());
		String dgUser = getSunLogin(authenticatedUserDto);
		String internalPropId = authenticatedUserDto.getProperty(organization, property).getInternalId();
		Property property1 = propertyService.findById(new PropertyId(organization, property));
		GuestId guestId = new GuestId(organization, property, resNumber, roomSequence, guestSequence);
		LeadContact leadContact = leadContactService.findFullyLoaded(guestId);
		TourDto result = (leadContact == null ? new TourDto() : new TourDto(leadContact, internalPropId));
		result.setProperty(new PropertyDto(property1));
		result.setPropId(String.valueOf(property));

		if (StringUtils.isBlank(result.getSalesCenter())) {
			List<PersonPropertyDto> pPropDto = authenticatedUserDto.getProperties().stream().filter(
					e -> Objects.equals(e.getProperty(), property) && Objects.equals(e.getOrganization(), organization))
					.collect(Collectors.toList());

			if (pPropDto != null && !pPropDto.isEmpty()) {
				result.setSalesCenter(pPropDto.get(0).getSalesCenter());
			}
		}

		if (leadContact != null) {
			if (!leadContact.getPackages().isEmpty()) {
				List<Package> packages = leadContact.getPackagesActiveWithin(leadContact.getArrivalDate());
				for (Package packageView : packages) {
					// Package packageView = new ArrayList<>(packages).get(0);
					if ("Preview".equalsIgnoreCase(packageView.getPackageType())) {
						result.setVendorTourNum(packageView.getPackageNumber());
						result.setVendorTourSrc(packageView.getPreferredInvitationNumber());
						String code = packageView.getCampaign();
						if (code != null) {
							LeadSource leadSource = leadSourceService.find(new LeadSourceId(organization, property,
									code.substring(0, Math.min(6, code.length()))));
							if (leadSource != null && leadSource.getId() != null) {
								result.setLeadSrcCd(leadSource.getId().getLeadSourceCode());
							}
						}
					}
				}
			}
			result.setGuestArrivalDate(leadContact.getArrivalDate());
			result.setGuestDepartureDate(leadContact.getDepartureDate());
			if (leadContact.getRateCode() != null) {
				Rate foundRate = rateService.find(new RateId(organization, property, leadContact.getRateCode()));
				if (foundRate != null && foundRate.getLeadSourceCode() != null) {
					result.setLeadSourceCode(foundRate.getLeadSourceCode());
				}
			}
			if (leadContact.getOwners() != null) {
				Set<Owner> owners = leadContact.getOwners();
				if (owners != null && !owners.isEmpty() && owners.toArray().length > 0) {
					result.setMemberNumber(owners.iterator().next().getId().getOwnerId());
				}
			}
			if (leadContact.getRateCode() != null) {
				result.setRatePlanID(leadContact.getRateCode());
				Rate foundRate = rateService.find(new RateId(organization, property, leadContact.getRateCode()));
				if (foundRate != null && foundRate.getRateName() != null) {
					result.setRatePlanName(foundRate.getRateName());
				}
			}
			if (leadContact.getArrivalDate() != null) {
				result.setGuestArrivalDate(leadContact.getArrivalDate());
			}
			if (leadContact.getGuest() != null) {
				if (result.getAddress() != null) {
					result.setSvoCreateUserId(getSunLogin(authenticatedUserDto));
				}
				result = applyCountryFix(result, leadContact.getGuest());
				if (leadContact.getGuest().getCountry() != null && leadContact.getGuest().getCountry().length() == 3) {
					Lookup lookup = lookUpService.find(new LookupId(organization, property, LookupService.STATECOUNTRY,
							leadContact.getGuest().getCountry()));
					if (lookup != null && lookup.getId() != null) {
						result.setCountryCd(lookup.getId().getLookupCode());
						result.getAddress().setStateCountryCd(lookup.getId().getLookupCode());
					}
				}

				if (leadContact.getGuest().getState() != null && leadContact.getGuest().getState().length() == 2) {
					Lookup lookup = lookUpService.find(new LookupId(organization, property, LookupService.STATECOUNTRY,
							leadContact.getGuest().getState()));
					if (lookup != null && lookup.getId() != null) {
						result.setStateCode(lookup.getId().getLookupCode());
						result.getAddress().setStateCountryCd(lookup.getId().getLookupCode());
					}
				}

				if (leadContact.getGuest().getEmailAddress() != null) {
					TourEmail tourEmail = new TourEmail(organization, null, leadContact.getGuest().getEmailAddress(),
							internalPropId);
					tourEmail.setCreatedBy(dgUser);
					tourEmail.setPrimaryFlag("Y");
					result.setTourEmails(Collections.singletonList(new TourEmailDto(tourEmail, internalPropId)));
				}
				if (leadContact.getGuest().getPmsConfirmationNum() != null) {
					result.setFolioNumber(leadContact.getGuest().getPmsConfirmationNum());
				}
				if (leadContact.getGuest().getPhoneNumber() != null) {
					result.getAddress().setHomePhoneNum(leadContact.getGuest().getPhoneNumber());
				}
			}
			if (leadContact.getId().getReservationConfirmationNum() != null) {
				result.setStarlinkConfirmationNumber(leadContact.getId().getReservationConfirmationNum());
			}
			if (leadContact.getStay() != null) {
				result.setHotelRoomNum(leadContact.getStay().getStayRoomNumber());
			}

		}
		return result;
	}

	private String getSunLogin(AuthenticatedUserDto user) {

		Map<String, String> logins = user.getLogins();
		if (logins != null && logins.containsKey(DG_USER)) {
			return logins.get(DG_USER);
		}
		return null;

	}

	/**
	 * Get tour emails
	 *
	 * @param organization
	 *            - Org code
	 * @param property
	 *            - Property id
	 * @param emailAddress
	 *            - The email address to search for.
	 * @param tripTicketNumber
	 *            - The trip ticket number
	 * @return - Tour Emails matching the search criteria
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping(value = "/email")
	public TourEmail getTourEmails(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) String property, String emailAddress,
			String tripTicketNumber) {

		return tourEmailService.find(new TourEmailId(organization, tripTicketNumber, emailAddress, property));

	}

	/**
	 * Returns a single trip ticket
	 *
	 * @param organization
	 *            - The org id
	 * @param property
	 *            - The property id
	 * @param tripTicket
	 *            - Trip ticket number
	 * @return TripTicket (TourDto) of a given trip ticket number.
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping(value = "/{tripTicket}")
	public TourDto getTripTicket(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property,
			@RequestParam(value = "alternateProperty", required = false) String alternateProperty,
			@PathVariable(value = "tripTicket") String tripTicket) {

		Property property1 = null;
		if (alternateProperty == null) {
			property1 = propertyService.findById(new PropertyId(organization, property));
		} else {
			List<Property> foundProps = propertyService.findByInternalPropertyIdAndOrganization(alternateProperty,
					organization);
			if (foundProps != null && !foundProps.isEmpty()) {
				Optional<Property> matchedProp = foundProps.stream()
						.filter(e -> Objects.equals(e.getId().getPropertyId(), property)).findFirst();
				property1 = matchedProp.orElseGet(() -> foundProps.get(0));
			}
		}
		if (property1 == null) {
			throw new InvalidClientRequest(
					"Property " + property + " could not be matched with an internal or external property.");
		}
		try {
			TripTicketList list = tourBookService.findTour(property1.getInternalPropertyId(), tripTicket);
			if (list.getTripTickets().size() > 0) {
				TripTicket ticket = list.getTripTickets().get(0);
				String originalTicket;
				originalTicket = jsonMapper.writeValueAsString(ticket);
				originalTicket = Base64.getEncoder().encodeToString(originalTicket.getBytes());
				TourId thisTour = new TourId(organization, ticket.getSvoPropId(), tripTicket);
				TourDto tourDto = new TourDto(ticket, thisTour, originalTicket);
				Tour persistedTour = tourBookService.findTour(thisTour);
				if (persistedTour != null) {
					tourDto.setOriginalTourType(persistedTour.getOriginalTourType());
				}
				tourDto.setProperty(new PropertyDto(property1));
				tourDto.setManifestDesc(
						getManifestDesc(organization, property1.getId().getPropertyId(), tourDto.getManifestCd()));
				return tourDto;

			} else {
				return new TourDto("Trip Ticket Not Found", "Trip Ticket Not Found");
			}
		} catch (JsonProcessingException e) {
			return new TourDto("Server Error", "ServerError");
		} catch (SunServiceException e) {
			return new TourDto(e.getFriendlyMessage(), e.getMessage());
		}

	}

	/**
	 * Sends form information to filter for specified Manifest Codes
	 *
	 * @param organization
	 *            - Org Id
	 * @param property
	 *            - Prop Id
	 * @param salesCenterId
	 *            - Sales Center Id
	 * @param startDate
	 *            - Start Date
	 * @param endDate
	 *            - End Date
	 * @return {@link List<TourManifestDto>}
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping(value = "/manifest/tripticket")
	public List<TourManifestDto> manifestListSearch(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @RequestParam String salesCenterId,
			@RequestParam(value = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(value = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

		if (salesCenterId == null) {
			throw new InvalidClientRequest("Missing Sales Center ID!");
		}
		if (startDate == null) {
			startDate = LocalDate.now();
		}
		Date manifestStartDate = java.sql.Date.valueOf(startDate);
		Date manifestEndDate = java.sql.Date.valueOf(endDate);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		AuthenticatedUserDto authenticatedUserDto = AuthenticatedUserDto.getUser(authentication.getPrincipal());

		TripTicketManifestList getManifestList = tourBookService.findTourAvailability(
				authenticatedUserDto.getProperty(organization, property).getInternalId(), salesCenterId,
				manifestStartDate, manifestEndDate);

		if (getManifestList.getManifests() == null) {
			return new ArrayList<>();
		}

		List<TourManifestDto> manifestList = getManifestList.getManifests().stream().map(TourManifestDto::new)
				.collect(Collectors.toList());

		return manifestList.stream().filter(map -> map.getNumBooked() <= map.getNumTotal())
				.collect(Collectors.toList());

	}

	/**
	 * Reschedules a trip ticket
	 *
	 * @param organization
	 *            - The organization id
	 * @param property
	 *            - The property id
	 * @param dto
	 *            - The TourDto
	 * @return the rescheduled tour containing xref information about the trip ticket numbers.
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping(value = "/reschedule")
	public TourDto rescheduleTour(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) String property, @RequestBody TourDto dto) {

		AuthenticatedUserDto user = AuthenticatedUserDto
				.getUser(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		return tourBookService.rescheduleTour(dto, organization, property, getSunLogin(user));
	}

	@PostMapping(value = "/local/search")
	public List<TourSearchResultDto> searchTour(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @RequestBody TourSearchDto dto) {

		AuthenticatedUserDto user = AuthenticatedUserDto
				.getUser(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		AuthenticatedOrganizationDto currentOrg = user.getOrganization(organization);

		String solicitorId = "";

		List<String> currSolicitorIds = currentOrg.getLogins().stream()
				.filter((login) -> login.getCode().equals("SOLICITORNUM")).map(AuthenticatedLoginDto::getUsername)
				.collect(Collectors.toList());

		if (currSolicitorIds.size() > 0) {
			solicitorId = currSolicitorIds.get(0);
		}

		AuthenticatedPropertyDto currentProp = user.getProperty(organization, property);

		Boolean canSearchAny = currentProp.getAccesses().contains(CoreConstants.ACCESS_TOURS_SEARCH);
		String internalId = currentProp.getInternalId();
		List<Lookup> mainfestCodes = lookUpService.findByIdOrganizationIdAndIdPropertyIdAndIdLookupCategoryCode(
				organization, property, LookupService.MANIFEST);
		List<Tour> searchTour = tourSearchService.tourSearch(dto, organization, internalId);
		Map<LookupId, Lookup> map = mainfestCodes.stream().collect(Collectors.toMap(Lookup::getId, item -> item));

		if (!canSearchAny) {
			final String solicId = solicitorId;
			searchTour = searchTour.stream()
					.filter((tour) -> tour.getSolicitor1() != null && tour.getSolicitor1().equals(solicId))
					.collect(Collectors.toList());
		}

		return searchTour.stream().map(TourSearchResultDto::new).collect(Collectors.toList()).stream().map(e -> {
			if (e != null && e.getTourManifestCode() != null) {
				Lookup foundLookup = map
						.get(new LookupId(organization, property, LookupService.MANIFEST, e.getTourManifestCode()));

				if (foundLookup != null && foundLookup.getLookupDesc() != null) {
					Long manifestText = parseManifestTime(foundLookup.getLookupDesc());
					if (!manifestText.equals(Long.MAX_VALUE)) {
						e.setSessionTime(foundLookup.getLookupDesc());
						e.setSessionTimeMills(manifestText);
					} else {
						e.setSessionTime(null);
						e.setSessionTimeMills(Long.MAX_VALUE);
					}

				}
			}
			return e;
		}).sorted(Comparator.comparing(
				tourSearchResultDto -> tourSearchResultDto != null && tourSearchResultDto.getSessionTimeMills() != null
						? tourSearchResultDto.getSessionTimeMills()
						: Long.MAX_VALUE))
				.collect(Collectors.toList());
	}

	private Long parseManifestTime(String manifestTime) {

		if (manifestTime != null) {
			String[] patterns = { "h:mm a", "h:mma" };
			Date result = DateUtils.parseDate(manifestTime, patterns);
			return result != null ? result.getTime() : Long.MAX_VALUE;
		} else {
			return Long.MAX_VALUE;
		}

	}

	private List<LookUpDto> sortFilterCountries(List<LookUpDto> combinedList) {

		List<LookUpDto> countries = combinedList.stream().filter(x -> x.getLookUpCode().length() == 3)
				.collect(Collectors.toList());
		countries.sort(Comparator.comparing(LookUpDto::getLookUpDesc));
		return countries;

	}

	private List<LookUpDto> sortFilterStates(List<LookUpDto> combinedList) {

		List<LookUpDto> states = combinedList.stream().filter(x -> x.getLookUpCode().length() == 2)
				.collect(Collectors.toList());
		states.sort(Comparator.comparing(LookUpDto::getLookUpDesc));
		return states;
	}

	/**
	 * Updates an existing trip ticket.
	 *
	 * @param organization
	 *            - The current organization
	 * @param property
	 *            - The current property
	 * @param dto
	 *            - TourDto which contains a base 64 encoded version of the original trip ticket.
	 * @return the updated trip ticket.
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping(value = "/update")
	public TourDto updateTicket(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) String property, @RequestBody TourDto dto) {

		AuthenticatedUserDto user = AuthenticatedUserDto
				.getUser(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		return tourBookService.modifyTour(dto, organization, property, getSunLogin(user));
	}

}
