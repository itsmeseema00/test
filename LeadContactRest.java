package com.vistana.onsiteconcierge.rest;

import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.FilterDropdownDto;
import com.vistana.onsiteconcierge.core.dto.GuestSearchDto;
import com.vistana.onsiteconcierge.core.dto.LeadContactDto;
import com.vistana.onsiteconcierge.core.dto.LeadStatusDto;
import com.vistana.onsiteconcierge.core.dto.LookUpDto;
import com.vistana.onsiteconcierge.core.dto.PersonDto;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.model.Lookup;
import com.vistana.onsiteconcierge.core.model.Person;
import com.vistana.onsiteconcierge.core.service.LeadContactService;
import com.vistana.onsiteconcierge.core.service.LeadStatusService;
import com.vistana.onsiteconcierge.core.service.LookupService;
import com.vistana.onsiteconcierge.core.service.PackageService;
import com.vistana.onsiteconcierge.core.service.PersonService;
import com.vistana.onsiteconcierge.core.service.PropertyService;
import com.vistana.onsiteconcierge.core.service.ReservationService;
import com.vistana.onsiteconcierge.core.service.StayService;
import com.vistana.onsiteconcierge.core.service.TourService;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leadcontact")
public class LeadContactRest {

    @Autowired
    protected LeadContactService leadContactService;

    @Autowired
    protected LeadStatusService leadStatusService;

    @Autowired
    protected LookupService lookupService;

    @Autowired
    protected PackageService packageService;

    @Autowired
    protected PropertyService propertyService;

    @Autowired
    protected PersonService personService;

    @Autowired
    protected ReservationService resService;

    @Autowired
    protected StayService stayService;

    @Autowired
    protected TourService tourService;

    @GetMapping(path = "/search/manager")
    public List<LeadContactDto> arrivalDateSearch(
        @RequestParam(value = CoreConstants.ORGANIZATION) String organization,
        @RequestParam(value = CoreConstants.PROPERTY) Integer property,
        @RequestParam(value = CoreConstants.START_DATE) @DateTimeFormat(pattern = CoreConstants.DATE_FORMAT) Date start,
        @RequestParam(value = CoreConstants.END_DATE) @DateTimeFormat(pattern = CoreConstants.DATE_FORMAT) Date end,
        @RequestParam(value = "isDeparture", required = false) Boolean isDeparture) {
        if (ChronoUnit.DAYS.between(start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) > 90) {
            throw new InvalidClientRequest();
        }

        List<LeadContact> leads = this.leadContactService
            .findByArrivalDeparture(organization, property, start,
                end, isDeparture);

        return mapLeadDtos(leads, organization, property);
    }


    private List<LeadContactDto> mapLeadDtos(List<LeadContact> leads, String organization,
        @RequestParam(value = CoreConstants.PROPERTY) Integer property) {
        final List<LeadContactDto> dtos = new ArrayList<>();
        if (leads.size() > 0) {
            List<Lookup> guestTypes = lookupService
                .getLookUpData(organization, LookupService.GUEST_TYPE, property);

            List<Person> persons = personService
                .findByOrganizationAndProperty(organization, property);
            Map<Integer, Person> mapPersons = persons.stream()
                .collect(Collectors.toMap(Person::getId, p -> p));

            leads.forEach(lead -> {
                lead.setGuestType(guestTypes);

                Person person = mapPersons.get(lead.getAllocatedPersonId());
                String name = "";
                if (person != null) {
                    name = person.getFirstName() + " " + person.getLastName();
                }
                LeadContactDto dto = new LeadContactDto(lead);
                dto.setAssignedVSCName(name);
                dtos.add(dto);
            });
        }

        return dtos;
    }

    @PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
    @PostMapping(value = "/searchGuest")
    public List<LeadContactDto> searchByGuest(
        @RequestParam(value = CoreConstants.ORGANIZATION) String organization,
        @RequestParam(value = CoreConstants.PROPERTY) Integer property,
        @RequestBody GuestSearchDto guestSearchDto) {

        return leadContactService
            .searchByGuest(guestSearchDto, property, organization);
    }


    @PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
    @GetMapping(path = "/search")
    public List<LeadContactDto> customerSearch(
        @RequestParam(value = CoreConstants.ORGANIZATION) String organization,
        @RequestParam(value = CoreConstants.PROPERTY) Integer property,
        @RequestParam(value = "query") String query) {

        List<LeadContact> results;

        if (NumberUtils.isNumber(query)) {
            results = this.leadContactService
                .currentGuestRoomNumberSearch(organization, property, query);
        } else {
            results = this.leadContactService.currentGuestNameSearch(organization, property, query);
        }

        return results.stream().map(LeadContactDto::new).collect(Collectors.toList());
    }

    @PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
    @GetMapping(path = "/manager/dashboard")
    public List<LeadContactDto> getCurrentGuests(
        @RequestParam(value = CoreConstants.ORGANIZATION) String organization,
        @RequestParam(value = CoreConstants.PROPERTY) Integer property) {

        List<LeadContact> leads = this.leadContactService.findFullyLoaded(organization, property);

        final List<LeadContactDto> dtos = new ArrayList<>();
        if (leads.size() > 0) {
            List<Lookup> guestTypes = lookupService
                .getLookUpData(organization, LookupService.GUEST_TYPE, property);

            List<Person> persons = personService
                .findByOrganizationAndProperty(organization, property);
            Map<Integer, Person> mapPersons = persons.stream()
                .collect(Collectors.toMap(Person::getId, p -> p));

            leads.forEach(lead -> {
                lead.setGuestType(guestTypes);

                Person person = mapPersons.get(lead.getAllocatedPersonId());
                String name = "";
                if (person != null) {
                    name = person.getFirstName() + StringUtils.SPACE + person.getLastName();
                }
                LeadContactDto dto = new LeadContactDto(lead);
                dto.setAssignedVSCName(name);
                dtos.add(dto);
            });
        }
        return dtos;
    }

    @PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
    @GetMapping(path = "/{userId}/dashboard")
    public List<LeadContactDto> getDashboard(
        @PathVariable(value = CoreConstants.USER_ID) Integer userId,
        @RequestParam(value = CoreConstants.ORGANIZATION) String organization,
        @RequestParam(value = CoreConstants.PROPERTY) Integer property) {

        List<LeadContact> leads = leadContactService
            .findFullyLoaded(organization, property, userId);

        final List<LeadContactDto> dtos = new ArrayList<>();
        if (leads.size() > 0) {
            List<Lookup> guestTypes = lookupService
                .getLookUpData(organization, LookupService.GUEST_TYPE, property);

            leads.forEach(lead -> {
                lead.setGuestType(guestTypes);
                dtos.add(new LeadContactDto(lead));
            });
        }
        return dtos;
    }

    @GetMapping(path = "/{userId}/search")
    public List<LeadContactDto> vscArrivalDateSearch(
        @RequestParam(value = CoreConstants.ORGANIZATION) String organization,
        @RequestParam(value = CoreConstants.PROPERTY) Integer property,
        @RequestParam(value = CoreConstants.START_DATE) @DateTimeFormat(pattern = CoreConstants.DATE_FORMAT) Date start,
        @RequestParam(value = CoreConstants.END_DATE) @DateTimeFormat(pattern = CoreConstants.DATE_FORMAT) Date end,
        @PathVariable(value = CoreConstants.USER_ID) Integer userId,
        @RequestParam(value = "isDeparture", required = false) Boolean isDeparture) {
        List<LeadContact> leads = leadContactService
            .findByArrivalDeparture(organization, property, start,
               end, userId, isDeparture);
        return mapLeadDtos(leads, organization, property);

    }

    /**
     * Returns filter dropdowns for the dashboard filter
     *
     * @param organization The current organization
     * @param property The current property
     * @return Filter Dropdowns
     */
    @PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
    @GetMapping(path = "/filter/dropdowns")
    public FilterDropdownDto getFilterDropdowns(
        @RequestParam(value = CoreConstants.ORGANIZATION) String organization,
        @RequestParam(value = CoreConstants.PROPERTY) Integer property) {
        List<PersonDto> vscs = personService
            .findByOrganizationAndPropertyAndActive(organization, property, true)
            .stream().map(PersonDto::new).collect(Collectors.toList());

        List<LeadStatusDto> leadStatus = leadStatusService
            .findAllByOrganizationIdAndPropertyId(organization, property)
            .stream().map(LeadStatusDto::new).collect(Collectors.toList());

        List<LookUpDto> guestTypes = lookupService
            .getLookUpData(organization, LookupService.GUEST_TYPE, property)
            .stream().map(LookUpDto::new).collect(Collectors.toList());

        return new FilterDropdownDto(guestTypes, leadStatus, vscs);
    }


}