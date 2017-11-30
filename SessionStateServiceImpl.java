package com.vistana.onsiteconcierge.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.SessionStateRepository;
import com.vistana.onsiteconcierge.core.model.SessionState;
import com.vistana.onsiteconcierge.core.service.SessionStateService;

@Service
public class SessionStateServiceImpl implements SessionStateService {

	@Autowired
	private SessionStateRepository repository;

	@Override
	public void delete(String entity) {
		repository.delete(entity);
	}

	@Override
	public SessionState find(String id) {

		return repository.find(id);
	}

	@Override
	public SessionState save(SessionState entity) {

		return repository.save(entity);
	}

}
