package com.vistana.onsiteconcierge.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.TimezoneRepository;
import com.vistana.onsiteconcierge.core.model.Timezone;
import com.vistana.onsiteconcierge.core.model.TimezoneId;
import com.vistana.onsiteconcierge.core.service.TimezoneService;

@Service
public class TimezoneServiceImpl extends SaveDeleteServiceImpl<Timezone, TimezoneId> implements TimezoneService {

	@Autowired
	private TimezoneRepository repository;

	@Override
	protected CrudRepository<Timezone, TimezoneId> getRepository() {

		return repository;
	}

}
