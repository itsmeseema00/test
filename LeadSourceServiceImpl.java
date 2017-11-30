package com.vistana.onsiteconcierge.core.service.impl;

import com.vistana.onsiteconcierge.core.dao.LeadSourceRepository;
import com.vistana.onsiteconcierge.core.model.LeadSource;
import com.vistana.onsiteconcierge.core.model.LeadSourceId;
import com.vistana.onsiteconcierge.core.service.LeadSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LeadSourceServiceImpl extends SaveDeleteServiceImpl<LeadSource, LeadSourceId> implements LeadSourceService {

	@Autowired
	private LeadSourceRepository repository;

	@Override
	public Set<LeadSource> findAll(String organization, Integer property) {

		return repository.findByIdOrganizationIdAndIdPropertyIdAndActiveFlagTrue(organization, property);
	}

	@Override
	protected CrudRepository<LeadSource, LeadSourceId> getRepository() {

		return repository;
	}

}
