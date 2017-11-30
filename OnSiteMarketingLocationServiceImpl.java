package com.vistana.onsiteconcierge.core.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.OnSiteMarketingLocationRepository;
import com.vistana.onsiteconcierge.core.model.OnSiteMarketingLocation;
import com.vistana.onsiteconcierge.core.service.OnSiteMarketingLocationService;

@Service
public class OnSiteMarketingLocationServiceImpl extends SaveDeleteServiceImpl<OnSiteMarketingLocation, Integer>
		implements OnSiteMarketingLocationService {

	@Autowired
	private OnSiteMarketingLocationRepository repo;

	@Override
	public List<OnSiteMarketingLocation> findAll(String organization, Integer property) {
		return repo.findByOrganizationIdAndPropertyIdOrderBySortOrder(organization, property);
	}

	@Override
	protected CrudRepository<OnSiteMarketingLocation, Integer> getRepository() {
		return repo;
	}
}
