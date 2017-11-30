package com.vistana.onsiteconcierge.core.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.AllocationRepository;
import com.vistana.onsiteconcierge.core.model.Allocation;
import com.vistana.onsiteconcierge.core.model.AllocationId;
import com.vistana.onsiteconcierge.core.model.QAllocation;
import com.vistana.onsiteconcierge.core.service.AllocationService;
import com.vistana.util.IterableHelper;

@Service
public class AllocationServiceImpl extends SaveDeleteServiceImpl<Allocation, AllocationId> implements AllocationService {

	@Autowired
	private AllocationRepository repository;

	@Override
	public List<Allocation> findByCategoryCode(String organizationId, Integer propertyId,
			String allocationCategoryCode) {

		QAllocation allocation = QAllocation.allocation;
		Iterable<Allocation> iterator = repository
				.findAll(
						allocation.id.allocationCategoryCode.eq(allocationCategoryCode)
								.and(allocation.id.organizationId.eq(organizationId)
										.and(allocation.id.propertyId.eq(propertyId))),
						allocation.id.allocationCategoryCode.asc());
		return IterableHelper.convertList(iterator);
	}

	@Override
	protected CrudRepository<Allocation, AllocationId> getRepository() {

		return repository;
	}

}
