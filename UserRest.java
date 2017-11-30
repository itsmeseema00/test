package com.vistana.onsiteconcierge.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.AccessDto;
import com.vistana.onsiteconcierge.core.dto.AllocationDto;
import com.vistana.onsiteconcierge.core.dto.PersonAccessDto;
import com.vistana.onsiteconcierge.core.dto.PersonAllocationDto;
import com.vistana.onsiteconcierge.core.dto.PersonPropertyDto;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.exception.InvalidEmailException;
import com.vistana.onsiteconcierge.core.model.Access;
import com.vistana.onsiteconcierge.core.model.Allocation;
import com.vistana.onsiteconcierge.core.model.Person;
import com.vistana.onsiteconcierge.core.model.PersonAccess;
import com.vistana.onsiteconcierge.core.model.PersonAccessId;
import com.vistana.onsiteconcierge.core.model.PersonAllocation;
import com.vistana.onsiteconcierge.core.model.PersonLogin;
import com.vistana.onsiteconcierge.core.model.PersonLoginId;
import com.vistana.onsiteconcierge.core.model.PersonProperty;
import com.vistana.onsiteconcierge.core.model.PersonPropertyId;
import com.vistana.onsiteconcierge.core.service.AccessService;
import com.vistana.onsiteconcierge.core.service.AllocationService;
import com.vistana.onsiteconcierge.core.service.LeadContactService;
import com.vistana.onsiteconcierge.core.service.LeadStatusService;
import com.vistana.onsiteconcierge.core.service.LookupService;
import com.vistana.onsiteconcierge.core.service.OrganizationService;
import com.vistana.onsiteconcierge.core.service.PersonAccessService;
import com.vistana.onsiteconcierge.core.service.PersonAllocationService;
import com.vistana.onsiteconcierge.core.service.PersonPropertyService;
import com.vistana.onsiteconcierge.core.service.PersonService;

@RestController
@RequestMapping("/user")
public class UserRest {

	private static Logger log = LoggerFactory.getLogger(UserRest.class);

	@Autowired
	protected AccessService accessService;

	@Autowired
	protected AllocationService allocationService;

	@Autowired
	protected LeadContactService leadContactService;

	@Autowired
	protected LeadStatusService leadStatusService;

	@Autowired
	protected OrganizationService orgService;

	@Autowired
	protected PersonAccessService personAccessService;

	@Autowired
	protected PersonAllocationService personAllocationService;

	@Autowired
	protected PersonPropertyService personPropertyService;

	@Autowired
	protected PersonService personService;

	@Autowired
	protected LookupService lookupService;

	private void findPersonLoginDiff(Person person, PersonLogin login, String userName, Set<PersonLogin> newLogins,
			Set<PersonLogin> removeLogins) {

		Predicate<PersonLogin> p = item -> item.equals(login);
		if (person.getPersonLogin() != null) {
			if (StringUtils.isEmpty(userName)) {
				person.getPersonLogin().stream().filter(p).findFirst().ifPresent(removeLogins::add);
			} else {
				login.setUserName(userName);

				Optional<PersonLogin> found = person.getPersonLogin().stream().filter(p).findFirst();
				if (found.isPresent() && !userName.equals(found.get().getUserName())) {
					found.get().setUserName(userName);
					newLogins.add(found.get());
				} else if (!found.isPresent()) {
					newLogins.add(login);
				}
			}
		} else if (!StringUtils.isEmpty(userName)) {
			login.setUserName(userName);
			newLogins.add(login);
		}

	}

	@PreAuthorize(Constants.ALLOWED_FOR_ADMINISTRATOR)
	@GetMapping("/{userId}/allocations")
	public PersonAllocationDto getAllocationByUser(@PathVariable(value = CoreConstants.USER_ID) Integer userId,
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		PersonAllocationDto personDto;
		Map<String, List<AllocationDto>> map;
		List<AllocationDto> allocationDtos = new ArrayList<>();
		Person person = personService.findWithAllocation(userId);
		if (person == null) {
			person = personService.findById(userId);
			personDto = new PersonAllocationDto(person);
			personDto.setAllocations(new HashMap<>());
		} else {
			allocationDtos = person.getPersonAllocation(organization, property).stream().map(AllocationDto::new)
					.collect(Collectors.toList());
			personDto = new PersonAllocationDto(person);
		}

		map = personDto.getAllocations();
		List<Allocation> guestTypes = allocationService.findByCategoryCode(organization, property,
				Constants.ALLOCATION_GUEST_TYPE);
		map.put(Constants.ALLOCATION_GUEST_TYPE, new ArrayList<>());
		List<AllocationDto> guestTypeDtos = map.get(Constants.ALLOCATION_GUEST_TYPE);
		guestTypes.forEach(guestType -> guestTypeDtos.add(new AllocationDto(userId, guestType)));

		List<Allocation> languages = allocationService.findByCategoryCode(organization, property,
				Constants.ALLOCATION_LANGUAGE);
		map.put(Constants.ALLOCATION_LANGUAGE, new ArrayList<>());
		List<AllocationDto> languageDtos = map.get(Constants.ALLOCATION_LANGUAGE);
		languages.forEach(language -> languageDtos.add(new AllocationDto(userId, language)));

		allocationDtos.forEach(item -> {
			AllocationDto found = null;
			if (guestTypeDtos.contains(item)) {
				found = guestTypeDtos.get(guestTypeDtos.indexOf(item));
			} else if (languageDtos.contains(item)) {
				found = languageDtos.get(languageDtos.indexOf(item));
			}

			if (found != null) {
				found.setActiveFlag(item.getActiveFlag());
			}
		});

		return personDto;
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping(path = "/persons")
	public List<PersonPropertyDto> getPersons(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		List<Person> persons = personService.findByOrganizationAndProperty(organization, property);
		return persons.stream().map(item -> new PersonPropertyDto(organization, property, item))
				.collect(Collectors.toList());
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ADMINISTRATOR)
	@GetMapping(path = "/{userId}/accesses")
	public PersonAccessDto getUser(@PathVariable(value = CoreConstants.USER_ID) Integer userId,
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		List<Access> accesses = accessService.findByOrganization(organization);
		Person person = personService.findWithAccessAndLoginAndProperty(userId);
		return new PersonAccessDto(organization, property, person, accesses);
	}

	@PreAuthorize(Constants.ALLOWED_FOR_CHANGE_USER_ACCESS)
	@PostMapping("/{userId}/allocations")
	public List<AllocationDto> savePersonAllocation(@PathVariable(value = CoreConstants.USER_ID) Integer userId,
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @RequestBody List<AllocationDto> dto) {

		Set<PersonAllocation> incomingAllocations = new HashSet<>();
		dto.forEach(incoming -> incomingAllocations.add(new PersonAllocation(organization, property, userId,
				incoming.getCode(), incoming.getCategory(), incoming.getActiveFlag())));
		List<PersonAllocation> existingAllocations = new ArrayList<>(
				personAllocationService.findAll(organization, property, userId));

		Set<PersonAllocation> save = new HashSet<>();
		incomingAllocations.forEach(incoming -> {
			if (existingAllocations.contains(incoming)) {
				PersonAllocation found = existingAllocations.get(existingAllocations.indexOf(incoming));
				found.setActiveFlag(incoming.getActiveFlag());
				save.add(found);
			} else if (incoming.getActiveFlag()) {
				save.add(incoming);
			}
		});

		return personAllocationService.save(save).stream().map(AllocationDto::new).collect(Collectors.toList());
	}

	@PreAuthorize(Constants.ALLOWED_FOR_CHANGE_USER_ACCESS)
	@PostMapping(path = "/{userId}/accesses")
	public Boolean setUser(@PathVariable(value = CoreConstants.USER_ID) Integer userId,
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @RequestBody PersonAccessDto dto) {
		boolean validEmail = false;
		if (!StringUtils.isEmpty(dto.getEmailAddress())) {
			String emailDomain = getEmailDomain(dto);
			validEmail = lookupService.isLookUpDescForEmail(organization, property, emailDomain);
		} else {
			validEmail = true;
		}
		
		Person person = null;
		Person temp = personService.findWithAccessAndLoginAndProperty(dto.getUserName());
		if (userId >= 0) {
			person = personService.findWithAccessAndLoginAndProperty(userId);
			updateEmail(dto, validEmail, person);
		} else if (temp == null
				&& (dto.getUserName() == null || dto.getFirstName() == null || dto.getLastName() == null)) {
			throw new InvalidClientRequest();
		} else if (!validEmail) {
			throw new InvalidEmailException();
		} else if (temp == null && validEmail) {
			person = personService
					.save(new Person(dto.getUserName(), dto.getFirstName(), dto.getLastName(), dto.getEmailAddress()));
		} else if (temp.getPersonProperty(organization, property) != null) {
			throw new InvalidClientRequest();
		} else {
			person = temp;
		}

		if (person == null) {
			log.info("User not found for ID: {}, UserName: {}", userId, dto.getUserName());
			throw new InvalidClientRequest();
		}

		Integer id = person.getId();

		Set<PersonLogin> newLogins = new HashSet<>();
		Set<PersonLogin> removeLogins = new HashSet<>();

		PersonLogin userlogin = new PersonLogin(
				new PersonLoginId(organization, id, Constants.LOGIN_TYPE_CODE_VSE_DG_USER));
		findPersonLoginDiff(person, userlogin, dto.getDgUserId(), newLogins, removeLogins);

		PersonLogin solicitorLogin = new PersonLogin(
				new PersonLoginId(organization, id, Constants.LOGIN_TYPE_CODE_VSE_SOLICITOR_NUM));
		findPersonLoginDiff(person, solicitorLogin, dto.getSolicitorId(), newLogins, removeLogins);

		List<AccessDto> dtoAccesses = dto.getAccesses();
		Set<PersonAccess> personAccesses = person.getPersonAccess(organization, property);

		Set<PersonAccess> newAccesses = new HashSet<>();
		Set<PersonAccess> removeAccesses = new HashSet<>();

		dtoAccesses.forEach(item -> {
			PersonAccess access = new PersonAccess(
					new PersonAccessId(organization, property, id, item.getAccessCode()));
			if (item.getActiveFlag() && !personAccesses.contains(access)) {
				newAccesses.add(access);
			} else if (!item.getActiveFlag() && personAccesses.contains(access)) {
				removeAccesses.add(access);
			}
		});

		PersonProperty currentProperty = person.getPersonProperty(organization, property);
		PersonProperty updatedProperty = null;
		if (currentProperty == null) {
			updatedProperty = new PersonProperty(new PersonPropertyId(organization, property, id));
			updatedProperty.setActiveFlag(dto.getActiveFlag());
			int newSortOrder = (person.getPersonProperty() == null) ? 1 : person.getPersonProperty().size() + 1;
			updatedProperty.setSortOrder(newSortOrder);
		} else if (currentProperty.getActiveFlag() != dto.getActiveFlag()) {
			currentProperty.setActiveFlag(dto.getActiveFlag());
			updatedProperty = currentProperty;
		}
		personService.save(updatedProperty, newLogins, removeLogins, newAccesses, removeAccesses);
		return true;
	}

	private void updateEmail(PersonAccessDto dto, boolean validEmail, Person person) {
		person.setEmailAddress(dto.getEmailAddress());
		//if (!StringUtils.isEmpty(person.getEmailAddress()) && validEmail) {
			if (validEmail) {
			personService.updatePersonEmail(person);
		} else if (!validEmail) {
			throw new InvalidEmailException();
		}
	}
	
	private String getEmailDomain(PersonAccessDto dto) {
		return '@' + dto.getEmailAddress().split("@")[1].toString().trim();
	}
}
