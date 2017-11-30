package com.vistana.onsiteconcierge.core.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.LeadGiftRepository;
import com.vistana.onsiteconcierge.core.model.LeadGift;
import com.vistana.onsiteconcierge.core.model.LeadGiftId;
import com.vistana.onsiteconcierge.core.service.LeadGiftService;

@Service
public class LeadGiftServiceImpl extends SaveDeleteServiceImpl<LeadGift, LeadGiftId> implements LeadGiftService {

	@Autowired
	private LeadGiftRepository repository;

	@Override
	public List<LeadGift> findLeadGift(String organizationId, Integer propertyId, String resNum) {

		return repository.findByIdOrganizationIdAndIdReservationConfirmationNum(organizationId, resNum);
	}

	@Override
	protected CrudRepository<LeadGift, LeadGiftId> getRepository() {

		return repository;
	}

}
