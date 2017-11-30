package com.vistana.onsiteconcierge.core.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.ActivatorRepository;
import com.vistana.onsiteconcierge.core.model.Activator;
import com.vistana.onsiteconcierge.core.service.ActivatorService;

@Service
public class ActivatorServiceImpl extends SaveDeleteServiceImpl<Activator, Integer> implements ActivatorService {

	@Autowired
	private ActivatorRepository repository;

	@Override
	public List<Activator> findByOrganizationIdAndPropertyId(String organizationId, Integer propertyId) {

		return (repository).findByOrganizationIdAndPropertyId(organizationId, propertyId);
	}

	@Override
	public List<Activator> findByOrganizationIdAndPropertyIdAndActiveFlagOrderBySortOrder(String organizationId,
			Integer propertyId, Boolean activeFlag) {

		return (repository).findByOrganizationIdAndPropertyIdAndActiveFlagOrderBySortOrder(organizationId, propertyId,
				activeFlag);
	}

	@Override
	protected CrudRepository<Activator, Integer> getRepository() {

		return repository;
	}

}
