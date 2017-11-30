package com.vistana.onsiteconcierge.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.TourGiftRepository;
import com.vistana.onsiteconcierge.core.model.TourGift;
import com.vistana.onsiteconcierge.core.model.TourSequenceId;
import com.vistana.onsiteconcierge.core.service.TourGiftService;

@Service
public class TourGiftServiceImpl extends SaveDeleteServiceImpl<TourGift, TourSequenceId> implements TourGiftService {

	@Autowired
	private TourGiftRepository repository;

	@Override
	protected CrudRepository<TourGift, TourSequenceId> getRepository() {

		return repository;
	}

}
