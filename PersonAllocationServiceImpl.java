package com.vistana.onsiteconcierge.core.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.vistana.onsiteconcierge.core.dao.PersonAllocationPredicates;
import com.vistana.onsiteconcierge.core.dao.PersonAllocationRepository;
import com.vistana.onsiteconcierge.core.dao.PersonPropertyPredicates;
import com.vistana.onsiteconcierge.core.model.PersonAllocation;
import com.vistana.onsiteconcierge.core.model.PersonAllocationId;
import com.vistana.onsiteconcierge.core.model.QPerson;
import com.vistana.onsiteconcierge.core.model.QPersonAllocation;
import com.vistana.onsiteconcierge.core.model.QPersonProperty;
import com.vistana.onsiteconcierge.core.service.PersonAllocationService;
import com.vistana.util.IterableHelper;

@Service
public class PersonAllocationServiceImpl extends SaveDeleteServiceImpl<PersonAllocation, PersonAllocationId>
		implements PersonAllocationService {

	@Autowired
	private PersonAllocationRepository repository;

	@Override
	public List<PersonAllocation> findActiveWithPerson(List<Integer> personIds, String organization, Integer property) {

		QPerson person = new QPerson("person");
		QPersonProperty personProperty = new QPersonProperty("personProperty");
		QPersonAllocation personAllocation = new QPersonAllocation("personAllocation");

		BooleanExpression expr = PersonPropertyPredicates
				.idByOrganizationProperty(personProperty, organization, property)
				.and(personProperty.activeFlag.eq(true))
				.and(PersonAllocationPredicates.idByOrganizationProperty(personAllocation, organization, property)
						.and(personAllocation.activeFlag.eq(true)));
		if (personIds != null && personIds.size() > 0) {
			expr = expr.and(personAllocation.id.personId.in(personIds));
		}

		return getQueryFactory().selectFrom(personAllocation).innerJoin(personAllocation.personProperty, personProperty)
				.leftJoin(personAllocation.person, person).fetchJoin().where(expr).distinct().fetch();
	}

	@Override
	public Set<PersonAllocation> findAll(String organizationId, Integer propertyId, Integer personId) {

		Iterable<PersonAllocation> iterator = repository
				.findByIdOrganizationIdAndIdPropertyIdAndIdPersonId(organizationId, propertyId, personId);
		return IterableHelper.convertSet(iterator);
	}

	@Override
	protected CrudRepository<PersonAllocation, PersonAllocationId> getRepository() {

		return repository;
	}

}
