package com.vistana.onsiteconcierge.core.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.GiftRepository;
import com.vistana.onsiteconcierge.core.model.Gift;
import com.vistana.onsiteconcierge.core.service.GiftService;

@Service
public class GiftServiceImpl extends SaveDeleteServiceImpl<Gift, Integer> implements GiftService {

	@Autowired
	private GiftRepository repository;

	@Override
	public Gift findByGiftTypeCode(String giftTypeCode) {

		return repository.findByGiftTypeCode(giftTypeCode);
	}

	@Override
	public List<Gift> findByOrganizationIdAndPropertyId(String organizationId, Integer propertyId) {

		return repository.findByOrganizationIdAndPropertyIdOrderBySortOrder(organizationId, propertyId);

	}

	@Override
	public List<Gift> findByOrganizationIdAndPropertyIdAndActiveFlag(String organizationId, Integer propertyId,
			Boolean activeFlag) {

		return repository.findByOrganizationIdAndPropertyIdAndActiveFlagOrderBySortOrder(organizationId, propertyId,
				activeFlag);
	}

	@Override
	protected CrudRepository<Gift, Integer> getRepository() {

		return repository;
	}

}
