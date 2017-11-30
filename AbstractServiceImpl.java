package com.vistana.onsiteconcierge.core.service.impl;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

@Transactional(readOnly = true)
public class AbstractServiceImpl<T, ID extends Serializable> {

	@PersistenceContext(unitName = "ConciergeApp")
	private EntityManager entityManager;

	@Autowired
	private JPAQueryFactory queryFactory;

	protected EntityManager getEntityManager() {

		return entityManager;
	}

	public JPAQueryFactory getQueryFactory() {

		return queryFactory;
	}

}
