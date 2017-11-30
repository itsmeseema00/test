package com.vistana.onsiteconcierge.core.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.vistana.onsiteconcierge.core.dao.LookupRepository;
import com.vistana.onsiteconcierge.core.model.Lookup;
import com.vistana.onsiteconcierge.core.model.LookupId;
import com.vistana.onsiteconcierge.core.model.QLookup;
import com.vistana.onsiteconcierge.core.model.QLookupCategory;
import com.vistana.onsiteconcierge.core.service.LookupService;

@Service
public class LookupServiceImpl extends SaveDeleteServiceImpl<Lookup, LookupId> implements LookupService {
	
	public static final String lookUpCategoryCode = "PERSONEMAILDOMAINS";

	@Autowired
	private LookupRepository repository;

	@Override
	public List<Lookup> findByIdOrganizationIdAndIdPropertyIdAndIdLookupCategoryCode(String organization,
			Integer property, String category) {

		return repository.findByIdOrganizationIdAndIdPropertyIdAndIdLookupCategoryCode(organization, property,
				category);
	}

	/**
	 * Takes an array of lookup codes and returns the lookup data.
	 *
	 * @param organizationId
	 * @param propertyId
	 * @param categoryCode
	 * @return
	 */
	@Override
	public Map<String, List<Lookup>> getLookUpData(String organizationId, int propertyId, String[] categoryCode) {

		QLookup lookUp = new QLookup("lookUp1");
		QLookupCategory category = new QLookupCategory("category1");

		BooleanExpression or1 = lookUp.id.propertyId.eq(propertyId).and(category.propertySpecificFlag.eq(true));
		BooleanExpression or2 = lookUp.id.propertyId.eq(0).and(category.propertySpecificFlag.eq(false));
		BooleanExpression orExpression = or1.or(or2);

		BooleanExpression expression = orExpression.and(lookUp.activeFlag.eq(true));

		List<Lookup> lookupList = new JPAQuery<Lookup>(getEntityManager()).from(lookUp)
				.innerJoin(lookUp.lookupCategory, category).where(lookUp.id.organizationId.eq(organizationId)
						.and(lookUp.id.lookupCategoryCode.in(categoryCode)).and(expression))
				.orderBy(lookUp.sortOrder.asc()).fetch();

		Map<String, List<Lookup>> returnList = new HashMap<>();
		Arrays.stream(categoryCode).forEach(e -> returnList.put(e, new ArrayList<>()));
		lookupList.forEach(l -> returnList.get(l.getLookupCategory().getId().getLookupCategoryCode()).add(l));
		return returnList;
	}

	@Override
	public List<Lookup> getLookUpData(String organizationId, String categoryCode, int propertyId) {

		QLookup lookUp = new QLookup("lookUp1");
		QLookupCategory category = new QLookupCategory("category1");
		BooleanExpression or1 = lookUp.id.propertyId.eq(propertyId).and(category.propertySpecificFlag.eq(true));
		BooleanExpression or2 = lookUp.id.propertyId.eq(0).and(category.propertySpecificFlag.eq(false));
		BooleanExpression orExpression = or1.or(or2);
		BooleanExpression expression = orExpression.and(lookUp.activeFlag.eq(true));
		return new JPAQuery<Lookup>(getEntityManager()).from(lookUp)
				.innerJoin(lookUp.lookupCategory, category).where(lookUp.id.organizationId.eq(organizationId)
						.and(lookUp.id.lookupCategoryCode.eq(categoryCode)).and(expression))
				.orderBy(lookUp.sortOrder.asc()).fetch();
	}

	@Override
	public List<Lookup> getManifestLookUpData(String organizationId, String categoryCode, int propertyId) {

		QLookup lookUp = new QLookup("lookUp1");
		return new JPAQuery<Lookup>(getEntityManager())
				.from(lookUp).where(lookUp.id.organizationId.eq(organizationId)
						.and(lookUp.id.lookupCategoryCode.eq(categoryCode)).and(lookUp.id.propertyId.eq(propertyId)))
				.orderBy(lookUp.id.lookupCode.asc()).fetch();
	}

	@Override
	protected CrudRepository<Lookup, LookupId> getRepository() {

		return repository;
	}

	@Override
	public boolean isLookUpDescForEmail(String organizationId, int propertyId, String LookupDesc) {

		List<Lookup> lookupList = new ArrayList<Lookup>();
		QLookup lookup = new QLookup("lookup");
		lookupList = getQueryFactory().selectFrom(lookup).where(lookup.id.organizationId.eq(organizationId)
				.and(lookup.id.propertyId.eq(propertyId).and(lookup.lookupDesc.eq(LookupDesc)))).fetch();

		return lookupList != null && !lookupList.isEmpty() && lookupList.get(0).getLookupDesc().equals(LookupDesc);
	}

	@Override
	public List<String> getLookUpDescList(String organizationId, int propertyId) {
		List<String> lookUpDescList = new ArrayList<String>();
		QLookup lookup = new QLookup("lookup");
		lookUpDescList = getQueryFactory().select(lookup.lookupDesc).from(lookup)
				.where(lookup.id.organizationId.eq(organizationId)
						.and(lookup.id.propertyId.eq(propertyId))
							.and(lookup.id.lookupCategoryCode.eq(lookUpCategoryCode)))
				.fetch();
		return lookUpDescList;
	}
}