package com.vistana.onsiteconcierge.core.service.impl;

import static com.vistana.onsiteconcierge.core.CoreConstants.PERIOD;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.vistana.util.IterableHelper;

public abstract class SaveDeleteServiceImpl<T, ID extends Serializable> extends FinderServiceImpl<T, ID> {

	@Transactional
	public void delete(Iterable<T> entities) {

		getRepository().delete(entities);
	}

	@Transactional
	public void delete(T entity) {

		getRepository().delete(entity);
	}

	protected Object findField(Object base, String path) {

		Object entity = null;
		try {
			Field field = base.getClass().getDeclaredField(path);
			field.setAccessible(true);
			entity = field.get(base);
		} catch (Exception e) {
			throw new IllegalStateException("Service error finding field: " + base + PERIOD + path);
		}
		return entity;
	}

	@Override
	abstract protected CrudRepository<T, ID> getRepository();

	@Transactional
	public Set<T> save(Iterable<T> entities) {

		Iterable<T> saved = getRepository().save(entities);
		return IterableHelper.convertSet(saved);
	}

	@Transactional
	public T save(T entity) {

		return getRepository().save(entity);
	}

}
