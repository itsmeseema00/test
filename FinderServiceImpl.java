package com.vistana.onsiteconcierge.core.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import com.vistana.util.IterableHelper;

public abstract class FinderServiceImpl<T, ID extends Serializable> extends AbstractServiceImpl<T, ID> {

	public T find(ID id) {

		return getRepository().findOne(id);
	}

	public Set<T> findAll() {

		return IterableHelper.convertSet(getRepository().findAll());
	}

	public List<T> findAll(Collection<ID> ids) {

		return (List<T>) getRepository().findAll(ids);
	}

	abstract protected CrudRepository<T, ID> getRepository();

}
