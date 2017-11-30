package com.vistana.onsiteconcierge.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.LookupCategoryRepository;
import com.vistana.onsiteconcierge.core.model.LookupCategory;
import com.vistana.onsiteconcierge.core.model.LookupCategoryId;
import com.vistana.onsiteconcierge.core.service.LookupCategoryService;

@Service
public class LookupCategoryServiceImpl extends SaveDeleteServiceImpl<LookupCategory, LookupCategoryId>
		implements LookupCategoryService {

	@Autowired
	private LookupCategoryRepository repository;

	@Override
	public boolean doesCategoryExist(String organization, String categoryName) {

		return this.find(new LookupCategoryId(organization, categoryName)) != null;
	}

	@Override
	protected CrudRepository<LookupCategory, LookupCategoryId> getRepository() {

		return repository;
	}

}