package com.vistana.onsiteconcierge.core.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.PersonAccessRepository;
import com.vistana.onsiteconcierge.core.model.PersonAccess;
import com.vistana.onsiteconcierge.core.model.PersonAccessId;
import com.vistana.onsiteconcierge.core.service.PersonAccessService;
import com.vistana.util.IterableHelper;

@Service
public class PersonAccessServiceImpl extends SaveDeleteServiceImpl<PersonAccess, PersonAccessId>
		implements PersonAccessService {

	@Autowired
	private PersonAccessRepository repository;

	@Override
	public Set<PersonAccess> findAll(String organizationId, Integer propertyId, Integer personId) {

		Iterable<PersonAccess> iterator = repository.findByIdOrganizationIdAndIdPropertyIdAndIdPersonId(organizationId,
				propertyId, personId);
		return IterableHelper.convertSet(iterator);
	}

	@Override
	protected CrudRepository<PersonAccess, PersonAccessId> getRepository() {

		return repository;
	}

}
