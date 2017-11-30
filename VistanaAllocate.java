package com.vistana.onsiteconcierge.core.service.impl;

import static com.vistana.onsiteconcierge.core.CoreConstants.ALLOCATION_GUEST_TYPE;
import static com.vistana.onsiteconcierge.core.CoreConstants.ALLOCATION_LANGUAGE;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.AllocationResultsDetailDto;
import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.model.Person;
import com.vistana.onsiteconcierge.core.model.PersonAllocation;
import com.vistana.onsiteconcierge.core.model.PersonAllocationId;
import com.vistana.onsiteconcierge.core.model.WorkSchedule;

public class VistanaAllocate {

	private Boolean ignoreWorkSchedule = false;

	private List<AllocationResultsDetailDto> leads = new ArrayList<>();

	Map<Date, Map<String, List<AllocationResultsDetailDto>>> newLeads = new HashMap<>();

	private Map<String, Set<Person>> mapGuestType = new HashMap<>();

	private Map<String, Set<Person>> mapLanguage = new HashMap<>();

	private Map<String, Set<Person>> mapWorkSchedule = new HashMap<>();

	private Map<Date, Map<Integer, Integer>> mapVscsCount = new HashMap<>();

	public VistanaAllocate(List<LeadContact> leads, List<PersonAllocation> allocations) {

		// calculating VSCs and number of times VSCs allocated to the leads in the given date range.
		Map<Date, Map<Integer, Integer>> personsAllocated = new HashMap<>();
		leads.forEach(lead -> {
			if (lead.getAllocatedPersonId() != null) {
				Map<Integer, Integer> personAndCount = personsAllocated.get(lead.getArrivalDate());
				if (personAndCount == null) {
					personAndCount = new HashMap<>();
					personsAllocated.put(lead.getArrivalDate(), personAndCount);
					personAndCount.put(lead.getAllocatedPersonId(), 1);
				} else {
					Integer count = 0;
					count = personAndCount.get(lead.getAllocatedPersonId()) == null ? 0
							: personAndCount.get(lead.getAllocatedPersonId());
					if (count > 0) {
						personAndCount.put(lead.getAllocatedPersonId(), count + 1);
					} else {
						personAndCount.put(lead.getAllocatedPersonId(), 1);
					}
				}
			}
		});

		List<Integer> personsToAllocate = allocations.stream().map(PersonAllocation::getId).collect(Collectors.toList())
				.stream().map(PersonAllocationId::getPersonId).collect(Collectors.toList());

		Map<Integer, Integer> personWeight = new HashMap<>();
		personsAllocated.entrySet().forEach(entry -> {
			entry.getValue().entrySet().forEach(subEntry -> {
				if (personsToAllocate.contains(subEntry.getKey())) {
					personWeight.put(subEntry.getKey(), subEntry.getValue());
				}
			});
			mapVscsCount.put(entry.getKey(), personWeight);
		});
	}

	public VistanaAllocate(List<LeadContact> leads, List<PersonAllocation> allocations, List<WorkSchedule> schedules,
			Boolean ignoreWorkSchedule) {

		this.leads = leads.stream().map(lead -> new AllocationResultsDetailDto(lead)).collect(Collectors.toList());

		Map<String, List<AllocationResultsDetailDto>> guestTypeMap = new HashMap<>();
		for (AllocationResultsDetailDto lead : this.leads) {
			List<AllocationResultsDetailDto> leadList;
			guestTypeMap = newLeads.get(lead.getStart());
			if (guestTypeMap != null && !guestTypeMap.isEmpty()) {
				if (guestTypeMap.containsKey(lead.getGuestType())) {
					guestTypeMap.get(lead.getGuestType()).add(lead);
				} else {
					leadList = new ArrayList<>();
					leadList.add(lead);
					guestTypeMap.put(lead.getGuestType(), leadList);
				}
			} else {
				leadList = new ArrayList<>();
				leadList.add(lead);
				guestTypeMap = new HashMap<>();
				guestTypeMap.put(lead.getGuestType(), leadList);
				newLeads.put(lead.getStart(), guestTypeMap);
			}
		}

		this.setIgnoreWorkSchedule(ignoreWorkSchedule);

		if (schedules != null) {
			schedules.forEach(schedule -> {
				String formatted = convertDate(java.sql.Date.valueOf(schedule.getId().getWorkDate()));
				Set<Person> persons = mapWorkSchedule.get(formatted);
				if (persons == null) {
					persons = new HashSet<>();
					mapWorkSchedule.put(formatted, persons);
				}
				persons.add(schedule.getPerson());
			});
		}

		allocations.forEach(allocation -> {

			// map per Guest Type and Language
			String category = allocation.getId().getAllocationCategoryCode();
			String code = allocation.getId().getAllocationCode();
			if (ALLOCATION_LANGUAGE.equals(category)) {

				Set<Person> persons = mapLanguage.get(code);
				if (persons == null) {
					persons = new HashSet<>();
					mapLanguage.put(code, persons);
				}
				persons.add(allocation.getPerson());
			} else if (ALLOCATION_GUEST_TYPE.equals(category)) {

				Set<Person> persons = mapGuestType.get(code);
				if (persons == null) {
					persons = new HashSet<>();
					mapGuestType.put(code, persons);
				}
				persons.add(allocation.getPerson());
			}
		});

	}

	public List<AllocationResultsDetailDto> assign() {

		this.leads.clear();

		newLeads.entrySet().forEach(perDate -> {
			perDate.getValue().entrySet().forEach(perGuestType -> {
				for (AllocationResultsDetailDto lead : perGuestType.getValue()) {

					String leadLanguage = lead.getLanguage();
					if (leadLanguage == null || (!leadLanguage.equals(CoreConstants.LANGUAGE_SPANISH)
							&& !leadLanguage.equals(CoreConstants.LANGUAGE_JAPANESE))) {
						// defaults to English
						lead.setLanguage(CoreConstants.LANGUAGE_ENGLISH);
					}
					if (lead.getGuestType() == null) {
						// defaults to blank
						lead.setGuestType(StringUtils.EMPTY);
					}

					String language = lead.getLanguage();
					String guestType = lead.getGuestType();
					String arrivalDate = convertDate(lead.getStart());

					Set<Person> languages = mapLanguage.get(language);
					Set<Person> guestTypes = mapGuestType.get(guestType);
					Set<Person> workSchedules = mapWorkSchedule.get(arrivalDate);

					Set<Person> intersection = intersection(languages, guestTypes, workSchedules);
					if (intersection.size() <= 0) {
						this.leads.add(lead);
						continue;
					}
					if (lead.getAssignment() == null) {
						Person next = findVsc(intersection, lead.getStart());

						lead.setAssignmentUserName(next.getUserName());
						lead.setAssignment(next.getPersonId());
					}
					this.leads.add(lead);
				}
			});
		});
		return leads;
	}

	private String convertDate(Date date) {

		return CoreConstants.DATE_FORMAT_SQL_FORMATTED.format(date);
	}

	private Person findVsc(Set<Person> persons, Date start) {

		List<Person> personsList = new ArrayList<>(persons);

		int randomNum = ThreadLocalRandom.current().nextInt(0, personsList.size());
		Person found = personsList.get(randomNum);

		Map<Integer, Integer> personAndCount = mapVscsCount.get(start) == null ? new HashMap<>()
				: mapVscsCount.get(start);
		int lowest = personAndCount.isEmpty() || personAndCount.get(found.getPersonId()) == null ? 0
				: personAndCount.get(found.getPersonId());

		for (Person one : personsList) {
			int count = personAndCount.get(one.getPersonId()) == null ? 0 : personAndCount.get(one.getPersonId());
			if (count < lowest) {
				found = one;
				lowest = count;
			}
		}
		personAndCount.put(found.getPersonId(),
				personAndCount.get(found.getPersonId()) == null ? 1 : personAndCount.get(found.getPersonId()) + 1);
		mapVscsCount.put(start, personAndCount);
		return found;
	}

	public Boolean getIgnoreWorkSchedule() {

		return ignoreWorkSchedule;
	}

	private Set<Person> intersection(Set<Person> languages, Set<Person> guestTypes, Set<Person> workSchedules) {

		Set<Person> intersection = new HashSet<>();
		if (languages == null || languages.size() <= 0) {
			// if language does not exist, no match since required
			return intersection;
		} else if (guestTypes == null || guestTypes.size() <= 0) {
			// if guest type does not exist, no match since required
			return intersection;
		} else if (this.getIgnoreWorkSchedule() == false && (workSchedules == null || workSchedules.size() <= 0)) {

			// if workSchedules does not exist, no match since required
			return intersection;
		}

		intersection = languages.stream().filter(guestTypes::contains).collect(Collectors.toSet());

		if (workSchedules != null) {
			// if work schedules exist, then match since not required
			intersection = intersection.stream().filter(workSchedules::contains).collect(Collectors.toSet());
		}
		return intersection;
	}

	public void setIgnoreWorkSchedule(Boolean ignoreWorkSchedule) {

		this.ignoreWorkSchedule = ignoreWorkSchedule;
	}
}
