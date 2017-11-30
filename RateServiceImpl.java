package com.vistana.onsiteconcierge.core.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.vistana.onsiteconcierge.core.dao.RateRepository;
import com.vistana.onsiteconcierge.core.model.QRate;
import com.vistana.onsiteconcierge.core.model.QReservation;
import com.vistana.onsiteconcierge.core.model.Rate;
import com.vistana.onsiteconcierge.core.model.RateId;
import com.vistana.onsiteconcierge.core.model.Reservation;
import com.vistana.onsiteconcierge.core.service.RateService;

@Service
public class RateServiceImpl extends SaveDeleteServiceImpl<Rate, RateId> implements RateService {

	@Autowired
	private RateRepository repository;

	@Override
	public Set<Rate> findAll(String organization, Integer property) {
		return repository.findByIdOrganizationIdAndIdPropertyId(organization, property);
	}

	@Override
	public List<Rate> findAllByOrganizationAndProperty(String organization, Integer property) {
		QRate rate = new QRate("rate1");
		BooleanExpression rateExpr = rate.id.organizationId.eq(organization).and(rate.id.propertyId.eq(property));

		return new JPAQuery<Rate>(getEntityManager()).from(rate).leftJoin(rate.createPerson).fetchJoin()
				.leftJoin(rate.updatePerson).fetchJoin().fetchJoin().distinct().where(rateExpr).fetch();
	}

	@Override
	public List<Rate> getRatesForGuestTypeMapping(String organizationId, int propertyId, Date start, Date end) {

		List<Rate> mappedCodes = this.getReservationsWithMappedRateCodes(organizationId, propertyId, start, end);
		List<Rate> unmappedCodes = this.getReservationsWithUnmappedRateCodes(organizationId, propertyId, start, end,
				mappedCodes);
		Set<Rate> _unmapped = unmappedCodes.stream().collect(Collectors.toSet());
		List<Rate> finalUnmappedCodes = new ArrayList<Rate>();
		finalUnmappedCodes.addAll(_unmapped);
		finalUnmappedCodes.addAll(finalUnmappedCodes.size(), mappedCodes);
		return finalUnmappedCodes;
	}

	@Override
	protected CrudRepository<Rate, RateId> getRepository() {

		return repository;
	}

	private List<Rate> getReservationsWithMappedRateCodes(String organizationId, int propertyId, Date start, Date end) {

		QRate rate = new QRate("rate1");
		BooleanExpression rateExpr = rate.id.organizationId.eq(organizationId).and(rate.id.propertyId.eq(propertyId));

		QReservation reservation = new QReservation("res1");
		BooleanExpression resExpr = reservation.arrivalDtm.between(start, end);
		BooleanExpression expression = rateExpr.and(resExpr);

		return new JPAQuery<Rate>(getEntityManager()).from(rate).leftJoin(rate.createPerson).fetchJoin()
				.leftJoin(rate.updatePerson).fetchJoin().rightJoin(rate.reservations, reservation).distinct()
				.where(expression).fetch();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Rate> getReservationsWithUnmappedRateCodes(String organizationId, int propertyId, Date start, Date end,
			List<Rate> mappedRateCodes) {

		List<Reservation> reservations = this.getEntityManager()
				.createQuery("SELECT DISTINCT R FROM Reservation R WHERE R.rateCode NOT IN "
						+ "(SELECT RA.id.rateCode FROM Rate RA WHERE RA.id.organizationId = :organization AND RA.id.propertyId = :property) "
						+ "AND R.id.organizationId = :organization " + "AND R.id.propertyId = :property "
						+ "AND R.arrivalDtm >= :start " + "AND R.arrivalDtm <= :end ", Reservation.class)
				.setParameter("organization", organizationId).setParameter("property", propertyId)
				.setParameter("start", start).setParameter("end", end).getResultList();
		// TODO: We need to either get rid of Hibernate or figure out how to
		// better manage our mappings and entities.
		/*
		 * reservations.forEach((res) -> { res.setRate(null); });
		 */

		return reservations.stream().map(Rate::new).collect(Collectors.toList());
	}

}
