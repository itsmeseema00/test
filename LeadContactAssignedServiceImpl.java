package com.vistana.onsiteconcierge.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.LeadContactAssignedRepository;
import com.vistana.onsiteconcierge.core.model.LeadContactAssigned;
import com.vistana.onsiteconcierge.core.service.LeadContactAssignedService;

@Service
public class LeadContactAssignedServiceImpl implements LeadContactAssignedService {

	@Autowired
	private LeadContactAssignedRepository repository;

	@Override
	public LeadContactAssigned find(String id) {

		return repository.find(id);
	}

	@Override
	public LeadContactAssigned save(LeadContactAssigned leadContactAssigned) {

		return repository.save(leadContactAssigned);
	}

}
