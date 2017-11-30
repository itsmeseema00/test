package com.vistana.onsiteconcierge.core.service.impl;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.vistana.onsiteconcierge.core.dao.GuestRepository;
import com.vistana.onsiteconcierge.core.dto.GuestSearchDto;
import com.vistana.onsiteconcierge.core.model.Guest;
import com.vistana.onsiteconcierge.core.model.GuestId;
import com.vistana.onsiteconcierge.core.model.QGuest;
import com.vistana.onsiteconcierge.core.model.QLeadContact;
import com.vistana.onsiteconcierge.core.model.QOrganization;
import com.vistana.onsiteconcierge.core.model.QProperty;
import com.vistana.onsiteconcierge.core.model.QStay;
import com.vistana.onsiteconcierge.core.service.GuestService;

import javax.persistence.TypedQuery;

@Service
public class GuestServiceImpl extends SaveDeleteServiceImpl<Guest, GuestId> implements
    GuestService {

  @Autowired
  private GuestRepository repository;

  @Override
  protected CrudRepository<Guest, GuestId> getRepository() {

    return repository;
  }

  @Override
  public Iterable<Guest> nonTransSave(List<Guest> guest) {

    return this.save(guest);
  }

  @Override
  @Transactional
  public Optional<List<Guest>> search(GuestSearchDto guestSearchDto,
      Integer internalProperty, String organization) {

    QGuest qGuest = new QGuest("GUEST");
    QLeadContact qLeadContact = new QLeadContact("LEADCONTACT");
    QStay qStay = new QStay("STAY");
    QProperty qProperty = new QProperty("PROPERTY");
    QOrganization qOrganization = new QOrganization("ORGANIZATION");
    BooleanBuilder tourSearchBuilder = new BooleanBuilder();

    if (guestSearchDto.getArrivalDateStart() != null
        && guestSearchDto.getArrivalDateEnd() != null) {
      Date start = Date
          .from(guestSearchDto.getArrivalDateStart().atStartOfDay(ZoneId.systemDefault())
              .toInstant());
      Date end = Date.from(
          guestSearchDto.getArrivalDateEnd().atStartOfDay(ZoneId.systemDefault()).toInstant());
      tourSearchBuilder.and(qLeadContact.arrivalDate.between(start, end));
    }

    if (!StringUtils.isBlank(guestSearchDto.getFirstName())) {
      tourSearchBuilder.and(qGuest.firstName.like(guestSearchDto.getFirstName()));
    }

    if (!StringUtils.isBlank(guestSearchDto.getLastName())) {
      tourSearchBuilder.and(qGuest.lastName.like(guestSearchDto.getLastName()));
    }

    if (!StringUtils.isBlank(guestSearchDto.getPmsConfirmation())) {
      tourSearchBuilder.and(qGuest.pmsConfirmationNum.like(guestSearchDto.getPmsConfirmation()));
    }
    if (!StringUtils.isBlank(guestSearchDto.getReservationConfirmation())) {
      tourSearchBuilder
          .and(qGuest.id.reservationConfirmationNum
              .like(guestSearchDto.getReservationConfirmation()));
    }

    tourSearchBuilder.and(qGuest.id.organizationId.eq(organization))
        .and(qGuest.id.propertyId.eq(internalProperty));

    List<Guest> searchResult = getQueryFactory().selectFrom(qGuest).innerJoin(qGuest.stay, qStay)
        .innerJoin(qGuest.property, qProperty).innerJoin(qGuest.organization, qOrganization)
        .where(tourSearchBuilder).fetchJoin().fetch();
    return Optional.ofNullable(searchResult);
  }

  public Optional<List<Guest>> searchLazy(GuestSearchDto guestSearchDto, Integer internalProperty,
      String organization) {

    String strQuery = "SELECT g FROM Guest g";

    if (guestSearchDto.getArrivalDateStart() != null
        && guestSearchDto.getArrivalDateEnd() != null) {

      strQuery += "AND g.ArrivalDate BETWEEN :startDate AND :endDate";
    }

    if (!StringUtils.isBlank(guestSearchDto.getFirstName())) {
      strQuery += "AND UPPER(g.FirstName) = :firstName";
    }

    if (!StringUtils.isBlank(guestSearchDto.getLastName())) {
      strQuery += "AND UPPER(g.LastName) = :lastName";
    }

    if (!StringUtils.isBlank(guestSearchDto.getPmsConfirmation())) {
      strQuery += "AND g.PMSConfirmationNum LIKE :pmsConfirmation";
    }
    if (!StringUtils.isBlank(guestSearchDto.getReservationConfirmation())) {
      strQuery += "AND g.ReservationConfirmationNum LIKE :resConfNum";

    }
    strQuery += "AND g.PropertyId LIKE :propId AND g.OrganizationId LIKE :orgId ORDER BY g.lastUpdateDtm";

    TypedQuery<Guest> query = getEntityManager().createQuery(strQuery, Guest.class);
    query.setParameter("orgId", organization);
    query.setParameter("propId", internalProperty);
    if (guestSearchDto.getArrivalDateStart() != null
        && guestSearchDto.getArrivalDateEnd() != null) {
      Date start = Date
          .from(guestSearchDto.getArrivalDateStart().atStartOfDay(ZoneId.systemDefault())
              .toInstant());
      Date end = Date.from(
          guestSearchDto.getArrivalDateEnd().atStartOfDay(ZoneId.systemDefault()).toInstant());
      query.setParameter("startDate", start);
      query.setParameter("endDate", end);
    }

    if (!StringUtils.isBlank(guestSearchDto.getFirstName())) {
      query.setParameter("firstName", guestSearchDto.getFirstName().toUpperCase());
    }

    if (!StringUtils.isBlank(guestSearchDto.getLastName())) {
      query.setParameter("lastName", guestSearchDto.getLastName().toUpperCase());
    }

    if (!StringUtils.isBlank(guestSearchDto.getPmsConfirmation())) {
      query.setParameter("pmsConfirmation", guestSearchDto.getPmsConfirmation());
    }
    if (!StringUtils.isBlank(guestSearchDto.getReservationConfirmation())) {
      query.setParameter("resConfNum", guestSearchDto.getReservationConfirmation());

    }

    List<Guest> searchResult = query.getResultList();
    return Optional.ofNullable(searchResult);
  }
}
