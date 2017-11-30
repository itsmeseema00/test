package com.vistana.onsiteconcierge.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.AllocationCategoryRepository;
import com.vistana.onsiteconcierge.core.model.AllocationCategory;
import com.vistana.onsiteconcierge.core.model.AllocationCategoryId;
import com.vistana.onsiteconcierge.core.service.AllocationCategoryService;

@Service
public class AllocationCategoryServiceImpl extends SaveDeleteServiceImpl<AllocationCategory, AllocationCategoryId>
		implements AllocationCategoryService {

	@Autowired
	private AllocationCategoryRepository repository;

	@Override
	protected CrudRepository<AllocationCategory, AllocationCategoryId> getRepository() {

		return repository;
	}

}
