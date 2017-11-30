package com.vistana.onsiteconcierge.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.AuditEventRepository;
import com.vistana.onsiteconcierge.core.model.AuditEvent;
import com.vistana.onsiteconcierge.core.service.AuditEventService;

@Service
public class AuditEventServiceImpl extends SaveDeleteServiceImpl<AuditEvent, Long> implements AuditEventService {

	@Autowired
	private AuditEventRepository repository;

	@Override
	protected CrudRepository<AuditEvent, Long> getRepository() {

		return repository;
	}

}
