package com.vistana.onsiteconcierge.core.service.impl;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vistana.onsiteconcierge.core.dao.PersonPropertyRepository;
import com.vistana.onsiteconcierge.core.dao.PersonRepository;
import com.vistana.onsiteconcierge.core.dto.PersonPropertyDto;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.model.Person;
import com.vistana.onsiteconcierge.core.model.PersonProperty;
import com.vistana.onsiteconcierge.core.model.PersonPropertyId;
import com.vistana.onsiteconcierge.core.model.QPersonProperty;
import com.vistana.onsiteconcierge.core.service.PersonPropertyService;
import com.vistana.util.IterableHelper;

@Service
public class PersonPropertyServiceImpl extends SaveDeleteServiceImpl<PersonProperty, PersonPropertyId>
		implements PersonPropertyService {
	private static final Logger log = Logger.getLogger(PersonPropertyServiceImpl.class.getName());
	@Autowired
	private PersonPropertyRepository repository;

	@Autowired
	private PersonRepository personRepository;

	@Override
	public Set<PersonProperty> findAll(String organizationId, Integer personId) {

		Iterable<PersonProperty> iterator = repository.findByIdOrganizationIdAndIdPersonId(organizationId, personId);
		return IterableHelper.convertSet(iterator);
	}

	public PersonPropertyDto getPersonProperty(String organization, Integer property, Integer personId) {

		if (personId != null) {
			PersonProperty personProperty = repository.findOne(new PersonPropertyId(organization, property, personId));
			Person person = personRepository.findOne(personId);
			return new PersonPropertyDto(personProperty, organization, property, person);
		} else {
			log.warning("No authenticated user found");
			throw new InvalidClientRequest("Cannot get authenticated user");
		}
	}

	public List<PersonProperty> findByOrganizationAndPropertyAndActive(String organizationId, Integer propertyId,
			Integer personId) {

		QPersonProperty personProperty = new QPersonProperty("personProperty");
		return getQueryFactory().selectFrom(personProperty)
				.where(personProperty.id.organizationId.eq(organizationId)
						.and(personProperty.id.propertyId.eq(propertyId).and(personProperty.id.personId.eq(personId))))
				.fetch();

	}

	@Override
	@Transactional
	public PersonPropertyDto updatePreferences(String organization, Integer property,
			PersonPropertyDto personPropertyDto, Integer personId) {

		Person person = personRepository.findOne(personId);
		PersonProperty personProperty = repository
				.findOne(new PersonPropertyId(organization, property, person.getId()));
		personProperty.setSalesCenter(personPropertyDto.getSalesCenter());
		personProperty.setLocation(personPropertyDto.getLocation());
		personProperty.setMainfestFilter(personPropertyDto.getMainfestFilter());
		return new PersonPropertyDto(personProperty, organization, property, person);
	}

	@Override
	protected CrudRepository<PersonProperty, PersonPropertyId> getRepository() {

		return repository;
	}

	@Override
	@Transactional
	public PersonProperty save(PersonProperty entity) {

		return this.repository.save(entity);
	}

}
