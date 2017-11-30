package com.vistana.onsiteconcierge.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.TourEmailRepository;
import com.vistana.onsiteconcierge.core.model.TourEmail;
import com.vistana.onsiteconcierge.core.model.TourEmailId;
import com.vistana.onsiteconcierge.core.service.TourEmailService;

@Service
public class TourEmailServiceImpl extends SaveDeleteServiceImpl<TourEmail, TourEmailId> implements TourEmailService {

	@Autowired
	private TourEmailRepository repository;

	@Override
	protected CrudRepository<TourEmail, TourEmailId> getRepository() {

		return repository;
	}

}
