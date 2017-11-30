package com.vistana.onsiteconcierge.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.StayRepository;
import com.vistana.onsiteconcierge.core.model.Stay;
import com.vistana.onsiteconcierge.core.service.StayService;

@Service
public class StayServiceImpl extends SaveDeleteServiceImpl<Stay, Integer> implements StayService {

	@Autowired
	private StayRepository repository;

	@Override
	public Stay findByStayId(Integer stayId) {

		return repository.findByStayId(stayId);
	}

	@Override
	protected CrudRepository<Stay, Integer> getRepository() {

		return repository;
	}

	@Override
	public Stay nonTransSave(Stay stay) {
		return this.repository.save(stay);
	}

}
