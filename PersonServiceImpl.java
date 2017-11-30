package com.vistana.onsiteconcierge.core.service.impl;

import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vistana.onsiteconcierge.core.dao.PersonAccessRepository;
import com.vistana.onsiteconcierge.core.dao.PersonAllocationPredicates;
import com.vistana.onsiteconcierge.core.dao.PersonLoginRepository;
import com.vistana.onsiteconcierge.core.dao.PersonPropertyPredicates;
import com.vistana.onsiteconcierge.core.dao.PersonPropertyRepository;
import com.vistana.onsiteconcierge.core.dao.PersonRepository;
import com.vistana.onsiteconcierge.core.dao.WorkSchedulePredicates;
import com.vistana.onsiteconcierge.core.model.Person;
import com.vistana.onsiteconcierge.core.model.PersonAccess;
import com.vistana.onsiteconcierge.core.model.PersonLogin;
import com.vistana.onsiteconcierge.core.model.PersonProperty;
import com.vistana.onsiteconcierge.core.model.QOrganization;
import com.vistana.onsiteconcierge.core.model.QPerson;
import com.vistana.onsiteconcierge.core.model.QPersonAllocation;
import com.vistana.onsiteconcierge.core.model.QPersonLogin;
import com.vistana.onsiteconcierge.core.model.QPersonProperty;
import com.vistana.onsiteconcierge.core.model.QProperty;
import com.vistana.onsiteconcierge.core.model.QWorkSchedule;
import com.vistana.onsiteconcierge.core.service.PersonService;
import com.vistana.util.DateHelper;

@Service
public class PersonServiceImpl extends SaveDeleteServiceImpl<Person, Integer> implements PersonService {

	@Autowired
	private PersonAccessRepository personAccessRepository;

	@Autowired
	private PersonLoginRepository personLoginRepository;

	@Autowired
	private PersonPropertyRepository personPropertyRepository;

	@Autowired
	private PersonRepository repository;

	@Override
	public Person findById(Integer id) {

		return repository.findOne(id);
	}

	@Override
	public List<Person> findByOrganizationAndProperty(String organizationId, Integer propertyId) {

		QPerson person = new QPerson("person");
		QPersonProperty personProperty = new QPersonProperty("personProperty");
		return getQueryFactory().selectFrom(person).innerJoin(person.personProperty, personProperty).fetchJoin()
				.where(PersonPropertyPredicates.idByOrganizationProperty(personProperty, organizationId, propertyId))
				.orderBy(person.lastName.asc(), person.firstName.asc()).fetch();
	}

	@Override
	public List<Person> findByOrganizationAndPropertyAndActive(String organization, Integer property, boolean active) {

		QPerson person = new QPerson("person");
		QPersonProperty personProperty = new QPersonProperty("personProperty");
		return getQueryFactory().selectFrom(person)
				.innerJoin(person.personProperty, personProperty).fetchJoin().where(PersonPropertyPredicates
						.idByOrganizationPropertyPersonActive(personProperty, organization, property, active))
				.orderBy(person.lastName.asc()).fetch();
	}

	@Override
	public Person findByUserName(String userName) {

		return repository.findByUserName(userName);
	}

	@Override
	public List<Person> findMarketingCommentsAllocated(Integer allocatedId, Integer initialContactedId,
			Integer initialAllocatedId) {

		QPerson person = new QPerson("person");
		return getQueryFactory().selectFrom(person).where(person.personId.eq(allocatedId)
				.or(person.personId.eq(initialContactedId).or(person.personId.eq(initialAllocatedId)))).fetch();
	}

	@Override
	public List<Person> findPropertyActiveWithAllocationActive(String organization, Integer property) {

		QPerson person = new QPerson("person");
		QPersonProperty personProperty = new QPersonProperty("personProperty");
		QPersonAllocation personAllocation = new QPersonAllocation("personAllocation");

		return getQueryFactory().selectFrom(person).innerJoin(person.personProperty, personProperty)
				.leftJoin(person.personAllocation, personAllocation).fetchJoin()
				.where(PersonPropertyPredicates.idByOrganizationProperty(personProperty, organization, property)
						.and(personProperty.activeFlag.eq(true))
						.and(PersonAllocationPredicates
								.idByOrganizationProperty(personAllocation, organization, property)
								.and(personAllocation.activeFlag.eq(true))))
				.distinct().fetch();
	}

	@Override
	public Person findWithAccessAndLoginAndProperty(Integer userId) {

		QPerson person = new QPerson("person");
		return getQueryFactory().selectFrom(person).leftJoin(person.personAccess).fetchJoin()
				.leftJoin(person.personLogin).fetchJoin().leftJoin(person.personProperty).fetchJoin()
				.where(person.personId.eq(userId)).fetchFirst();
	}

	@Override
	public Person findWithAccessAndLoginAndProperty(String userName) {

		QPerson person = new QPerson("person");
		return getQueryFactory().selectFrom(person).leftJoin(person.personAccess).fetchJoin()
				.leftJoin(person.personLogin).fetchJoin().leftJoin(person.personProperty).fetchJoin()
				.where(QPerson.person.userName.eq(userName)).fetchOne();
	}

	@Override
	public Person findWithAccessAndPropertyAndLogin(String userName) {

		QPerson person = new QPerson("person");
		QPersonProperty personProperty = new QPersonProperty("personProperty");
		QProperty property = new QProperty("property");
		QOrganization personPropertyOrganization = new QOrganization("personPropertyOrganization");

		QPersonLogin personLogin = new QPersonLogin("personLogin");
		QOrganization personLoginOrganization = new QOrganization("personLoginOrganization");
		Person foundPerson = getQueryFactory().selectFrom(person).leftJoin(person.personAccess).fetchJoin()
				.leftJoin(person.personProperty, personProperty).fetchJoin().leftJoin(personProperty.property, property)
				.fetchJoin().leftJoin(property.organization, personPropertyOrganization).fetchJoin()
				.leftJoin(person.personLogin, personLogin).fetchJoin()
				.leftJoin(personLogin.organization, personLoginOrganization).fetchJoin()
				.where(QPerson.person.userName.eq(userName)).fetchOne();

		if (foundPerson != null && foundPerson.getPersonLogin() != null) {
			Set<PersonLogin> loaded = new HashSet<>();
			foundPerson.getPersonLogin().forEach(one -> {
				if (one.getId() != null) {
					loaded.add(one);
				}
			});
			foundPerson.setPersonLogin(loaded);
		}

		return foundPerson;
	}

	@Override
	public Person findWithAllocation(Integer userId) {

		QPerson person = new QPerson("PERSON");
		return getQueryFactory().selectFrom(person).innerJoin(person.personAllocation).fetchJoin()
				.where(person.personId.eq(userId)).fetchOne();
	}

	@Override
	public List<Person> findWithWorkSchedule(String organization, Integer property, Date start, Date end) {

		QPerson person = new QPerson("person");
		QWorkSchedule workSchedule = new QWorkSchedule("workSchedule");

		Date startZeroed = DateHelper.dateZeroTime(start);
		Date endZeroed = DateHelper.dateZeroTime(end);
		return getQueryFactory().selectFrom(person).rightJoin(person.workSchedules, workSchedule).fetchJoin()
				.where(WorkSchedulePredicates.idByOrganizationProperty(workSchedule, organization, property)
						.and(workSchedule.id.workDate
								.goe(startZeroed.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
								.and(workSchedule.id.workDate
										.lt(endZeroed.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()))))
				.distinct().fetch();
	}

	@Override
	protected CrudRepository<Person, Integer> getRepository() {

		return repository;
	}
	
	@Override
	@Transactional
	public void save(PersonProperty property, Set<PersonLogin> newLogins, Set<PersonLogin> removeLogins,
			Set<PersonAccess> newAccesses, Set<PersonAccess> removeAccesses) {

		if (property != null) {
			personPropertyRepository.save(property);
		}
		if (newAccesses != null && !newAccesses.isEmpty()) {
			personAccessRepository.save(newAccesses);
		}
		if (removeAccesses != null && !removeAccesses.isEmpty()) {
			personAccessRepository.delete(removeAccesses);
		}
		if (newLogins != null && !newLogins.isEmpty()) {
			personLoginRepository.save(newLogins);
		}
		if (removeLogins != null && !removeLogins.isEmpty()) {
			personLoginRepository.delete(removeLogins);
		}
	}
	
	@Override
	public void updatePersonEmail(Person personObj) {
		QPerson person = new QPerson("PERSON");
		getQueryFactory().update(person).where(person.personId.eq(personObj.getId())).set(person.emailAddress,personObj.getEmailAddress()).execute();
	}
	
	
	
}
