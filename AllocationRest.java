package com.vistana.onsiteconcierge.rest;

import static java.time.temporal.ChronoUnit.DAYS;

import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.config.service.AllocateVscRequestDto;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.AllocationDropDownDto;
import com.vistana.onsiteconcierge.core.dto.AllocationResultsDetailDto;
import com.vistana.onsiteconcierge.core.dto.AllocationResultsDto;
import com.vistana.onsiteconcierge.core.dto.AllocationSearchDto;
import com.vistana.onsiteconcierge.core.dto.AuthenticatedUserDto;
import com.vistana.onsiteconcierge.core.dto.DropDownDto;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.exception.SecurityAccessException;
import com.vistana.onsiteconcierge.core.model.GuestId;
import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.model.LeadContactAssigned;
import com.vistana.onsiteconcierge.core.model.LeadStatus;
import com.vistana.onsiteconcierge.core.model.Lookup;
import com.vistana.onsiteconcierge.core.model.Person;
import com.vistana.onsiteconcierge.core.model.PersonAllocation;
import com.vistana.onsiteconcierge.core.model.WorkSchedule;
import com.vistana.onsiteconcierge.core.service.LeadContactAssignedService;
import com.vistana.onsiteconcierge.core.service.LeadContactService;
import com.vistana.onsiteconcierge.core.service.LeadStatusService;
import com.vistana.onsiteconcierge.core.service.LookupService;
import com.vistana.onsiteconcierge.core.service.PersonAllocationService;
import com.vistana.onsiteconcierge.core.service.PersonService;
import com.vistana.onsiteconcierge.core.service.WorkScheduleService;
import com.vistana.onsiteconcierge.core.service.impl.VistanaAllocate;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AllocationRest {
	private static final Boolean FALSE = false;
	private static final Logger LOGGER = Logger.getLogger(AllocationRest.class.getName());

	@Autowired
	private LeadContactAssignedService leadContactAssignedService;

	@Autowired
	private LeadContactService leadContactService;

	@Autowired
	LeadStatusService leadStatusService;

	@Autowired
	LookupService lookUpService;

	@Autowired
	private PersonAllocationService personAllocationService;

	@Autowired
	PersonService personService;

	@Autowired
	private WorkScheduleService workScheduleService;

	@PreAuthorize(Constants.ALLOWED_FOR_ALLOCATE)
	@PostMapping("/allocation/vscs")
	public AllocationResultsDto allocateVsc(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @RequestBody AllocateVscRequestDto leads) {

		List<AllocationResultsDetailDto> assigned;
		List<LeadContact> leadContacts;

		leadContacts = leads.getLeads().stream().map(lead -> new LeadContact(lead, organization, property))
				.collect(Collectors.toList());

		List<Integer> vscs = leads.getVscs();
		List<PersonAllocation> allocations = personAllocationService.findActiveWithPerson(vscs, organization, property);

		new VistanaAllocate(leadContacts, allocations);
		VistanaAllocate helper = new VistanaAllocate(leadContacts, allocations, null, true);
		assigned = helper.assign();
		// guid fix
		if (assigned.size() > 0) {
			String uuid = UUID.randomUUID().toString();

			int index = 1;
			for (AllocationResultsDetailDto assignment : assigned) {
				assignment.setGuid(uuid);
				assignment.setIndex(index);
				LeadContactAssigned newSave = new LeadContactAssigned(uuid, index, organization, property, assignment);
				leadContactAssignedService.save(newSave);
				index++;
			}

		}
		return new AllocationResultsDto(assigned);
	}

	/**
	 * Returns allocations and leads between a time range, number of guest type
	 * allocations and language allocations for visualizing VSE allocations
	 * inside a AllocationResultsDto
	 *
	 * @param organization
	 *            - The current organization
	 * @param property
	 *            - The current property number
	 * @param dto
	 *            - The Allocation Search Dto
	 * @return {@link AllocationResultsDto}
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ALLOCATE)
	@PostMapping("/allocation/search")
	public AllocationResultsDto allocationSearch(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @RequestBody AllocationSearchDto dto) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		AuthenticatedUserDto authenticatedUserDto = AuthenticatedUserDto.getUser(authentication.getPrincipal());
		LocalDate arrival = dto.getArrival();
		LocalDate departure = dto.getDeparture();
		Period between = Period.between(arrival, departure);
		if (arrival.isAfter(departure) || between.get(DAYS) > 90) {
			throw new InvalidClientRequest();
		}
		if (dto.isReDerive()) {
			try {
				leadContactService.callRederive(organization, property, arrival, departure,
						authenticatedUserDto.getId());
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Failure calling redrive: ", e);

			}
		}
		// DO NOT DELETE THIS - leadsComplete is used to calculate the weight of
		// every allocated VSC

		List<LeadContact> leadsComplete = leadContactService.findByArrivalDeparture(organization, property,
				java.sql.Date.valueOf(dto.getArrival()), java.sql.Date.valueOf(dto.getDeparture()), false);
		List<LeadContact> leads = leadContactService.findAllocatable(organization, property, dto);

		List<PersonAllocation> allocations = personAllocationService.findActiveWithPerson(dto.getAssignments(),
				organization, property);
		List<WorkSchedule> schedules = workScheduleService.findWithPerson(dto.getAssignments(), organization, property,
				java.sql.Date.valueOf(arrival), java.sql.Date.valueOf(departure));

		// DO NOT DELETE THIS - leadsComplete is used to calculate the weight of
		// every allocated VSC
		new VistanaAllocate(leadsComplete, allocations);
		VistanaAllocate helper = new VistanaAllocate(leads, allocations, schedules, false);
		List<AllocationResultsDetailDto> assigned = helper.assign();

		if (assigned.size() > 0) {
			// temporary save since we do not have caching
			String uuid = UUID.randomUUID().toString();

			int index = 1;
			for (AllocationResultsDetailDto assignment : assigned) {
				assignment.setGuid(uuid);
				assignment.setIndex(index);

				LeadContactAssigned newSave = new LeadContactAssigned(uuid, index, organization, property, assignment);
				leadContactAssignedService.save(newSave);
				index++;
			}
		}

		return new AllocationResultsDto(assigned);
	}

	/**
	 * Returns data for the page Drop-downs on the Allocation Search page
	 *
	 * @param organization
	 * @param property
	 * @return {@link AllocationDropDownDto}
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping("/allocation/dropdown")
	public AllocationDropDownDto getAllocationSearchDropDowns(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		List<Person> persons = this.personService.findByOrganizationAndPropertyAndActive(organization, property, true);
		List<DropDownDto> vscs = persons.stream().map(p -> new DropDownDto(p.getId().toString(), p.getFullName()))
				.collect(Collectors.toList());

		List<Lookup> lookUps = this.lookUpService.getLookUpData(organization, LookupService.GUEST_TYPE, property);
		List<DropDownDto> guestType = lookUps.stream()
				.map(lk -> new DropDownDto(lk.getId().getLookupCode(), lk.getLookupDesc()))
				.collect(Collectors.toList());

		Set<LeadStatus> statuses = this.leadStatusService.findAllByOrganizationIdAndPropertyId(organization, property);
		List<DropDownDto> leadStatuses = statuses.stream()
				.map(ls -> new DropDownDto(ls.getId().getLeadStatusCode(), ls.getLeadStatusDesc()))
				.collect(Collectors.toList());

		return new AllocationDropDownDto(guestType, leadStatuses, vscs);
	}

	/**
	 * Saves Allocation Data that's been updated on the Allocation Search
	 * Results page
	 *
	 * @param organization
	 *            - The current organization
	 * @param property
	 *            - The 4 digit property number
	 * @param details
	 *            - Allocation results details
	 * @return {@link AllocationResultsDetailDto}
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ALLOCATE)
	@PostMapping("/allocation/save")
	public List<AllocationResultsDetailDto> saveAllocations(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property,
			@RequestBody List<AllocationResultsDetailDto> details) throws SQLException {

		String uuid = details.get(0).getGuid();
		StopWatch watch = new StopWatch();
		watch.start();
		Map<String, AllocationResultsDetailDto> mapAssignedDetail = new HashMap<>();
		Map<GuestId, String> mapGuestLead = new HashMap<>();
		for (AllocationResultsDetailDto detail : details) {
			if (!uuid.equals(detail.getGuid())) {
				throw new InvalidClientRequest();
			}
			LeadContactAssigned assign = leadContactAssignedService
					.find(new LeadContactAssigned(uuid, detail.getIndex()).getId());
			mapAssignedDetail.put(new LeadContactAssigned(uuid, detail.getIndex()).getId(), detail);
			if (!assign.getOrganizationId().equals(organization) || !assign.getPropertyId().equals(property)) {
				throw new SecurityAccessException();
			}

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUserDto user = AuthenticatedUserDto.getUser(authentication.getPrincipal());
			List<String> accesses = user.getProperty(organization, property).getAccesses();

			if (!assign.getGuestTypeCode().equals(detail.getGuestType())
					&& !accesses.contains(CoreConstants.ACCESS_CHANGE_GUEST_TYPE)) {
				throw new SecurityAccessException();
			}

			if (assign.getAllocatedPersonId() != null && !assign.getAllocatedPersonId().equals(detail.getAssignment())
					&& !accesses.contains(CoreConstants.ACCESS_REASSIGN_VSC)) {
				throw new SecurityAccessException();
			}
			mapGuestLead.put(new GuestId(assign), assign.getId());
		}

		Optional<AllocationResultsDetailDto> earliest = details.stream().min(Comparator.comparing((lead) -> {
			return lead.getStart();
		}));

		Optional<AllocationResultsDetailDto> latest = details.stream().max(Comparator.comparing((lead) -> {
			return lead.getStart();
		}));

		Date latestAdj = DateUtils.addDays(latest.get().getStart(), 1);

		List<LeadContact> leads = leadContactService.findByArrivalDeparture(organization, property,
				earliest.get().getStart(), latestAdj, false);

		leads = leads.stream().filter((lead) -> {
			return mapGuestLead.get(lead.getId()) != null;
		}).collect(Collectors.toList());

		StopWatch forWatch = new StopWatch();
		while (leads.size() > 0) {
			forWatch.start();

			// List<LeadContact> leads = new ArrayList<>();
			List<LeadContact> batch = new ArrayList<>(shrinkTo(leads));
			StopWatch lcsWatch = new StopWatch();
			lcsWatch.start();
			lcsWatch.stop();
			LOGGER.log(Level.INFO,
					"ASTIME Find Lazy Records - Elapsed time in seconds: " + lcsWatch.getTotalTimeSeconds());

			StopWatch feWatch = new StopWatch();

			batch.forEach(lead -> {
				feWatch.start();
				String assignId = mapGuestLead.get(lead.getId());
				AllocationResultsDetailDto detail = mapAssignedDetail.get(assignId);

				lead.setAllocatedPersonId(detail.getAssignment());
				if (lead.getInitialAllocatedPersonId() == null) {
					lead.setInitialAllocatedPersonId(detail.getAssignment());
				}

				if (StringUtils.isBlank(detail.getGuestType())) {
					lead.setGuestTypeCode(null);
				} else if (!detail.getGuestType().equals(lead.getGuestTypeCode())) {
					lead.setGuestTypeCode(detail.getGuestType());
					lead.setOverrideGuestTypeFlag(true);

				}

				if (StringUtils.isBlank(detail.getLeadStatus())) {
					lead.setLeadStatusCode(null);
				} else if (!detail.getLeadStatus().equals(lead.getLeadStatusCode())) {
					lead.setLeadStatusCode(detail.getLeadStatus());
					lead.setOverrideLeadStatusFlag(true);

				}

				// org.springframework.orm.jpa.JpaSystemException: Found shared
				// references to collection
				lead.setRemarksText("Updated Allocations", FALSE);
				lead.setOwners(null);
				lead.setPackages(null);
				lead.setLinkedTours(null);
				feWatch.stop();
				LOGGER.log(Level.INFO,
						"ASTIME Processing Loop - Elapsed time in seconds: " + feWatch.getTotalTimeSeconds());
			});
			forWatch.stop();
			LOGGER.log(Level.INFO,
					"ASTIME Processing Records - Elapsed time in seconds: " + forWatch.getTotalTimeSeconds());
			leadContactService.saveNative(batch);
			leads.removeAll(batch);
		}
		watch.stop();
		LOGGER.log(Level.INFO, "ASTIME Total Allocation - Elapsed time in seconds: " + watch.getTotalTimeSeconds());
		return details;
	}

	void setLeadContactAssignedService(LeadContactAssignedService leadContactAssignedService) {

		this.leadContactAssignedService = leadContactAssignedService;
	}

	private List<LeadContact> shrinkTo(List<LeadContact> ids) {

		int batchSize = 1200;
		if (ids.size() > batchSize) {
			return ids.subList(0, batchSize);
		}
		return ids;

	}

}
