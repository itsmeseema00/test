package com.vistana.onsiteconcierge.core.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.LookupPropertyRepository;
import com.vistana.onsiteconcierge.core.model.LookupId;
import com.vistana.onsiteconcierge.core.model.LookupProperty;
import com.vistana.onsiteconcierge.core.service.LookupPropertyService;

@Service
public class LookupPropertyServiceImpl extends FinderServiceImpl<LookupProperty, LookupId>
		implements LookupPropertyService {

	@Autowired
	private LookupPropertyRepository repository;

	@Override
	public List<LookupProperty> findByCategory(String organizationId, Integer propertyId, String lookUpCategoryCode) {

		return repository.findByIdOrganizationIdAndIdPropertyIdAndIdLookupCategoryCode(organizationId, propertyId,
				lookUpCategoryCode);
	}

	@Override
	public LookupProperty findByLookupCode(String organizationId, Integer propertyId, String lookUpCategoryCode,
			String lookUpCode) {

		return repository.findByIdOrganizationIdAndIdPropertyIdAndIdLookupCategoryCodeAndIdLookupCode(organizationId,
				propertyId, lookUpCategoryCode, lookUpCode);
	}

	@Override
	protected CrudRepository<LookupProperty, LookupId> getRepository() {

		return repository;
	}

}
