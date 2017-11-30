package com.vistana.onsiteconcierge.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.AuditEventTypeRepository;
import com.vistana.onsiteconcierge.core.model.AuditEventType;
import com.vistana.onsiteconcierge.core.service.AuditEventTypeService;

@Service
public class AuditEventTypeServiceImpl extends SaveDeleteServiceImpl<AuditEventType, Integer>
		implements AuditEventTypeService {

	@Autowired
	private AuditEventTypeRepository repository;

	@Override
	protected CrudRepository<AuditEventType, Integer> getRepository() {

		return repository;
	}

	@Override
	public AuditEventType findByAuditEventTypeDesc(String description) {

		return repository.findByAuditEventTypeDesc(description);
	}

}
