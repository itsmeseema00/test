package com.vistana.onsiteconcierge.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.TourXrefRepository;
import com.vistana.onsiteconcierge.core.model.TourXref;
import com.vistana.onsiteconcierge.core.model.TourXrefId;
import com.vistana.onsiteconcierge.core.service.TourXrefService;

@Service
public class TourXrefServiceImpl extends SaveDeleteServiceImpl<TourXref, TourXrefId> implements TourXrefService {

	@Autowired
	private TourXrefRepository repository;

	@Override
	protected CrudRepository<TourXref, TourXrefId> getRepository() {

		return repository;
	}

}
