package com.vistana.onsiteconcierge.core.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.AccessRepository;
import com.vistana.onsiteconcierge.core.model.Access;
import com.vistana.onsiteconcierge.core.model.AccessId;
import com.vistana.onsiteconcierge.core.model.QAccess;
import com.vistana.onsiteconcierge.core.service.AccessService;
import com.vistana.util.IterableHelper;

@Service
public class AccessServiceImpl extends SaveDeleteServiceImpl<Access, AccessId> implements AccessService {

	@Autowired
	private AccessRepository repository;

	@Override
	public List<Access> findByOrganization(String organizationId) {

		QAccess access = QAccess.access;
		Iterable<Access> iterator = repository.findAll(access.id.organizationId.eq(organizationId),
				access.id.accessCode.asc());
		return IterableHelper.convertList(iterator);
	}

	@Override
	protected CrudRepository<Access, AccessId> getRepository() {

		return repository;
	}

}
