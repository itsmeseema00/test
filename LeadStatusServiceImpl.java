package com.vistana.onsiteconcierge.core.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.LeadStatusRepository;
import com.vistana.onsiteconcierge.core.model.LeadStatus;
import com.vistana.onsiteconcierge.core.model.LeadStatusId;
import com.vistana.onsiteconcierge.core.service.LeadStatusService;

@Service
public class LeadStatusServiceImpl extends SaveDeleteServiceImpl<LeadStatus, LeadStatusId>
		implements LeadStatusService {

	@Autowired
	private LeadStatusRepository repo;

	@Override
	public Set<LeadStatus> findAllByOrganizationIdAndPropertyId(String organizationId, Integer propertyId) {

		return this.repo.findByIdOrganizationIdAndIdPropertyIdOrderBySortOrder(organizationId, propertyId);
	}

	@Override
	public Set<LeadStatus> findAllByOrganizationIdAndPropertyId(String organizationId, Integer propertyId,
			Boolean activeFlag) {

		return this.repo.findByIdOrganizationIdAndIdPropertyIdAndActiveFlagOrderBySortOrder(organizationId, propertyId,
				activeFlag);
	}

	@Override
	public LeadStatus findAllByOrganizationIdAndPropertyIdLeadStatusCode(String organizationId, Integer propertyId,
			String leadStatusCode) {

		return this.repo.findByIdOrganizationIdAndIdPropertyIdAndIdLeadStatusCode(organizationId, propertyId,
				leadStatusCode);
	}

	@Override
	protected CrudRepository<LeadStatus, LeadStatusId> getRepository() {

		return repo;
	}

}
