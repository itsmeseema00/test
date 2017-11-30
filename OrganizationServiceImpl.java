package com.vistana.onsiteconcierge.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.OrganizationRepository;
import com.vistana.onsiteconcierge.core.model.Organization;
import com.vistana.onsiteconcierge.core.service.OrganizationService;

@Service
public class OrganizationServiceImpl extends SaveDeleteServiceImpl<Organization, String> implements OrganizationService {

	@Autowired
	private OrganizationRepository repository;

	@Override
	public Organization findById(String id) {

		return repository.findOne(id);
	}

	@Override
	protected CrudRepository<Organization, String> getRepository() {

		return repository;
	}

}
