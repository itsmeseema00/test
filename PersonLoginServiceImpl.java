package com.vistana.onsiteconcierge.core.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.PersonLoginRepository;
import com.vistana.onsiteconcierge.core.model.PersonLogin;
import com.vistana.onsiteconcierge.core.model.PersonLoginId;
import com.vistana.onsiteconcierge.core.service.PersonLoginService;
import com.vistana.util.IterableHelper;

@Service
public class PersonLoginServiceImpl extends SaveDeleteServiceImpl<PersonLogin, PersonLoginId> implements PersonLoginService {

	@Autowired
	private PersonLoginRepository repository;

	@Override
	public Set<PersonLogin> findAll(String organizationId, Integer personId) {

		Iterable<PersonLogin> iterator = repository.findByIdOrganizationIdAndIdPersonId(organizationId, personId);
		return IterableHelper.convertSet(iterator);
	}

	@Override
	protected CrudRepository<PersonLogin, PersonLoginId> getRepository() {

		return repository;
	}

}
