package com.vistana.onsiteconcierge.core.service.impl;



import com.vistana.onsiteconcierge.core.service.impl.FinderServiceImpl;
import com.vistana.util.IterableHelper;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public abstract class SaveServiceImpl<T, ID extends Serializable> extends FinderServiceImpl<T, ID> {
	protected Object findField(Object base, String path) {
		Object entity = null;

		try {
			Field e = base.getClass().getDeclaredField(path);
			e.setAccessible(true);
			entity = e.get(base);
			return entity;
		} catch (Exception arg4) {
			throw new IllegalStateException("Service error finding field: " + base + "." + path);
		}
	}

	protected abstract CrudRepository<T, ID> getRepository();

	@Transactional
	public Set<T> save(Iterable<T> entities) {
		Iterable<T> saved = this.getRepository().save(entities);
		return IterableHelper.convertSet(saved);
	}
}



