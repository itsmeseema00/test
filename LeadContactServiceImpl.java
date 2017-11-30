package com.vistana.onsiteconcierge.core.service.impl;

import static com.vistana.onsiteconcierge.core.CoreConstants.AUDIT_EVENT_TYPE_TOUR_SYNC;
import static com.vistana.onsiteconcierge.core.CoreConstants.TOUR_SYNC_REMARKS_TEXT;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dao.LeadContactRepository;
import com.vistana.onsiteconcierge.core.dto.AllocationSearchDto;
import com.vistana.onsiteconcierge.core.dto.GuestSearchDto;
import com.vistana.onsiteconcierge.core.dto.LeadContactDto;
import com.vistana.onsiteconcierge.core.model.AuditEvent;
import com.vistana.onsiteconcierge.core.model.AuditEventType;
import com.vistana.onsiteconcierge.core.model.Guest;
import com.vistana.onsiteconcierge.core.model.GuestId;
import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.model.LeadContactHistory;
import com.vistana.onsiteconcierge.core.model.LeadGift;
import com.vistana.onsiteconcierge.core.model.LeadStatus;
import com.vistana.onsiteconcierge.core.model.LeadStatusId;
import com.vistana.onsiteconcierge.core.model.Lookup;
import com.vistana.onsiteconcierge.core.model.Owner;
import com.vistana.onsiteconcierge.core.model.OwnerContract;
import com.vistana.onsiteconcierge.core.model.OwnerInventory;
import com.vistana.onsiteconcierge.core.model.Package;
import com.vistana.onsiteconcierge.core.model.PackageGift;
import com.vistana.onsiteconcierge.core.model.Person;
import com.vistana.onsiteconcierge.core.model.Property;
import com.vistana.onsiteconcierge.core.model.PropertyId;
import com.vistana.onsiteconcierge.core.model.QGuest;
import com.vistana.onsiteconcierge.core.model.QLeadContact;
import com.vistana.onsiteconcierge.core.model.QLeadStatus;
import com.vistana.onsiteconcierge.core.model.QPerson;
import com.vistana.onsiteconcierge.core.model.QProperty;
import com.vistana.onsiteconcierge.core.model.QStay;
import com.vistana.onsiteconcierge.core.model.QTour;
import com.vistana.onsiteconcierge.core.model.Stay;
import com.vistana.onsiteconcierge.core.model.Timezone;
import com.vistana.onsiteconcierge.core.model.TimezoneId;
import com.vistana.onsiteconcierge.core.model.Tour;
import com.vistana.onsiteconcierge.core.model.TourGift;
import com.vistana.onsiteconcierge.core.service.AuditEventService;
import com.vistana.onsiteconcierge.core.service.AuditEventTypeService;
import com.vistana.onsiteconcierge.core.service.GuestService;
import com.vistana.onsiteconcierge.core.service.LeadContactHistoryService;
import com.vistana.onsiteconcierge.core.service.LeadContactService;
import com.vistana.onsiteconcierge.core.service.LeadStatusService;
import com.vistana.onsiteconcierge.core.service.LookupService;
import com.vistana.onsiteconcierge.core.service.PersonService;
import com.vistana.onsiteconcierge.core.service.PropertyService;
import com.vistana.onsiteconcierge.core.service.TimezoneService;
import com.vistana.util.DateHelper;
import com.vistana.util.IterableHelper;

@Service
public class LeadContactServiceImpl extends SaveDeleteServiceImpl<LeadContact, GuestId> implements LeadContactService {

	public class LeadContactRowMapper implements RowMapper {

		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

			LeadContact leadContact = new LeadContact();
			leadContact.setId(new GuestId(rs.getString("OrganizationId"), rs.getInt("PropertyId"),
					rs.getString("ReservationConfirmationNum"), rs.getInt("RoomSequence"), rs.getInt("GuestSequence")));
			leadContact.setCustomerUniqueId(rs.getInt("CustomerUniqueId"));
			if (rs.wasNull()) {
				leadContact.setCustomerUniqueId(null);
			}
			leadContact.setStayId(rs.getInt("StayId"));
			if (rs.wasNull()) {
				leadContact.setStayId(null);
			}
			leadContact.setGuestStatus(rs.getString("GuestStatus"));
			leadContact.setRateCode(rs.getString("RateCode"));
			leadContact.setGuestTypeCode(rs.getString("GuestTypeCode"));
			leadContact.setInternalPropertyId(rs.getString("InternalPropertyId"));
			leadContact.setTripTicketNumber(rs.getString("TripTicketNumber"));
			leadContact.setLanguage(rs.getString("Language"));
			leadContact.setArrivalDate(rs.getDate("ArrivalDate"));
			leadContact.setDepartureDate(rs.getDate("DepartureDate"));
			leadContact.setOverrideGuestTypeFlag(rs.getBoolean("OverrideGuestTypeFlag"));
			leadContact.setContactTypeCode(rs.getString("ContactTypeCode"));
			leadContact.setActivatorId(rs.getInt("ActivatorId"));
			if (rs.wasNull()) {
				leadContact.setActivatorId(null);
			}
			leadContact.setLeadStatusCode(rs.getString("LeadStatusCode"));
			leadContact.setOverrideLeadStatusFlag(rs.getBoolean("OverrideLeadStatusFlag"));
			leadContact.setContactFlag(rs.getBoolean("ContactFlag"));
			leadContact.setInvitedFlag(rs.getBoolean("InvitedFlag"));
			leadContact.setShowOnTripTicketFlag(rs.getBoolean("ShowOnTripTicketFlag"));
			leadContact.setPromiseGiftFlag(rs.getBoolean("PromiseGiftFlag"));
			leadContact.setIssueGiftFlag(rs.getBoolean("IssueGiftFlag"));
			leadContact.setOnsiteMarketingLocationId(rs.getInt("OnsiteMarketingLocationId"));
			if (rs.wasNull()) {
				leadContact.setOnsiteMarketingLocationId(null);
			}
			leadContact.setRemarksText(rs.getString("RemarksText"), false);
			leadContact.setInitialAllocatedPersonId(rs.getInt("InitialAllocatedPersonId"));
			if (rs.wasNull()) {
				leadContact.setInitialAllocatedPersonId(null);
			}
			leadContact.setAllocatedPersonId(rs.getInt("AllocatedPersonId"));
			if (rs.wasNull()) {
				leadContact.setAllocatedPersonId(null);
			}
			leadContact.setInitialContactedPersonId(rs.getInt("InitialContactedPersonId"));
			if (rs.wasNull()) {
				leadContact.setInitialContactedPersonId(null);
			}
			leadContact.setEnhancedFlag(rs.getBoolean("EnhancedFlag"));
			leadContact.setActiveFlag(rs.getBoolean("ActiveFlag"));
			leadContact.setOdsUpdateDtm(rs.getDate("ODSUpdateDtm"));
			leadContact.setOdsPackageId(rs.getLong("ODSPackageId"));
			leadContact.setUpdateDtm(rs.getDate("UpdateDtm"));
			leadContact.setCreatePersonId(rs.getInt("CreatePersonId"));
			if (rs.wasNull()) {
				leadContact.setCreatePersonId(null);
			}
			leadContact.setCreateDtm(rs.getDate("CreateDtm"));
			leadContact.setConciergeSequenceId(rs.getLong("ConciergeSequenceId"));
			if (rs.wasNull()) {
				leadContact.setConciergeSequenceId(null);
			}
			leadContact.setSeed(rs.getBoolean("Seed"));
			leadContact.setOptInFlag(rs.getBoolean("OptInFlag"));
			leadContact.setOptInLastRefreshDtm(rs.getDate("OptInLastRefreshDtm"));
			leadContact.setBookingDtm(rs.getDate("BookingDtm"));
			leadContact.setRoomName(rs.getString("RoomName"));
			leadContact.setRoomNumber(rs.getString("RoomNumber"));
			return leadContact;
		}

	}

	private static final String COUT_RES = "COUT";
	private static final String CXL_RES = "CXL";
	private static final String DNA_RES = "DNA";
	private static final Boolean FALSE = false;
	private static final Logger LOGGER = Logger.getLogger(LeadContactServiceImpl.class.getName());
	private static final String TOUR_SYNC_UPDATE = "Tour update: %s";
	private static final Boolean TRUE = true;
	@Autowired
	private AuditEventService auditEventService;
	@Autowired
	private AuditEventTypeService auditEventTypeService;
	@Autowired
	private GuestService guestService;
	private JdbcTemplate jdbcTemplate; // Autowired with setter
	@Autowired
	private LeadContactHistoryService leadContactHistoryService;
	@Autowired
	private LeadContactRepository leadContactRepository;
	@Autowired
	private LeadContactService leadContactService;
	@Autowired
	private LeadStatusService leadStatusSerivce;
	@Autowired
	private PersonService personService;
	@Autowired
	private LookupService lookupService;
	@Autowired
	private PropertyService propertyService;

	@Autowired
	private TimezoneService timezoneService;

	@Transactional
	@Override
	public void callRederive(String organizationId, Integer propertyId, LocalDate start, LocalDate end,
			Integer personId) {

		Integer updatedRows = 0;
		Session session = getEntityManager().unwrap(Session.class);
		try {
			org.hibernate.Query spcQuery = session
					.createSQLQuery(
							"EXECUTE dbo.GuestTypeRederive @Org=:org, @PropertyId=:prop, @ArrivalDateStart=:startDate, @ArrivalDateEnd=:endDate, @PersonId=:person")
					.setParameter("org", organizationId).setParameter("prop", propertyId)
					.setParameter("startDate", new SimpleDateFormat("yyyy-MM-dd").parse(start.toString()))
					.setParameter("endDate", new SimpleDateFormat("yyyy-MM-dd").parse(end.toString()))
					.setParameter("person", personId);
			updatedRows = spcQuery.executeUpdate();

		} catch (ParseException e) {
			e.printStackTrace();
		}
		LOGGER.log(Level.INFO, "Performed redrived, updated: " + updatedRows);

	}

	@Override
	public List<LeadContact> currentGuestNameSearch(String organizationId, Integer propertyId, String name) {
		Date todayZero = DateHelper.dateZeroTime(getTZCorrectedDate(new Date(), propertyId, organizationId));

		QLeadContact lead = new QLeadContact("LEAD");
		QStay stay = new QStay("STAY");
		QGuest guest = new QGuest("GUEST");
		BooleanExpression stayExpr = lead.arrivalDate.loe(todayZero).and(lead.departureDate.goe(todayZero))
				.and(guest.firstName.startsWithIgnoreCase(name).or(guest.lastName.startsWithIgnoreCase(name)));

		Expression<?>[] guestProjections = { guest.firstName, guest.lastName, guest.guestStatus, guest.state,
				guest.arrivalTime, guest.spgTier, guest.pmsConfirmationNum, guest.addressLine1, guest.addressLine2,
				guest.addressLine3, guest.country, guest.zipCode, guest.emailAddress, guest.city, guest.middleInitial,
				guest.phoneNumber };
		Expression<?>[] stayProjections = { stay.stayLengthOfStay, stay.stayRoomRate, stay.stayRoomNumber,
				stay.stayRateCode };

		// http://stackoverflow.com/questions/33959641/querydsl-null-entity-from-onetomany-association-with-projection-bean
		Map<GuestId, LeadContact> transform = new JPAQuery<LeadContact>(getEntityManager()).from(lead)
				.leftJoin(lead.guest, guest).leftJoin(lead.stay, stay)
				.where(lead.id.organizationId.eq(organizationId).and(lead.id.propertyId.eq(propertyId)).and(stayExpr))
				.where(lead.guestStatus.notEqualsIgnoreCase(CXL_RES).and(lead.guestStatus.notEqualsIgnoreCase(DNA_RES)))
				.transform(GroupBy.groupBy(lead.id)
						.as(Projections.bean(LeadContact.class, lead.id, lead.guestTypeCode, lead.leadStatusCode,
								lead.stayId, lead.tripTicketNumber, lead.customerUniqueId, lead.allocatedPersonId,
								lead.contactFlag, lead.invitedFlag, lead.arrivalDate, lead.departureDate, lead.roomName,
								lead.roomNumber, Projections.bean(Guest.class, guestProjections).as("guest"),
								Projections.bean(Stay.class, stayProjections).as("stay"))));
		return new ArrayList<>(transform.values());
	}

	@Override
	public List<LeadContact> currentGuestRoomNumberSearch(String organizationId, Integer propertyId, String roomNum) {

		Date todayZero = DateHelper.dateZeroTime(getTZCorrectedDate(new Date(), propertyId, organizationId)); // FIXME
																												// Rollback
																												// date
																												// here

		QLeadContact lead = new QLeadContact("LEAD");
		QGuest guest = new QGuest("GUEST");
		QStay stay = new QStay("STAY");
		BooleanExpression stayExpr = lead.arrivalDate.loe(todayZero).and(lead.departureDate.goe(todayZero))
				.and(lead.roomNumber.startsWithIgnoreCase(roomNum));

		Expression<?>[] guestProjections = { guest.firstName, guest.lastName, guest.guestStatus, guest.state,
				guest.arrivalTime, guest.spgTier, guest.pmsConfirmationNum, guest.addressLine1, guest.addressLine2,
				guest.addressLine3, guest.country, guest.zipCode, guest.emailAddress, guest.city, guest.middleInitial,
				guest.phoneNumber };
		Expression<?>[] stayProjections = { stay.stayRoomNumber };

		// http://stackoverflow.com/questions/33959641/querydsl-null-entity-from-onetomany-association-with-projection-bean
		Map<GuestId, LeadContact> transform = new JPAQuery<LeadContact>(getEntityManager()).from(lead)
				.leftJoin(lead.guest, guest).leftJoin(lead.stay, stay)
				.where(lead.id.organizationId.eq(organizationId).and(lead.id.propertyId.eq(propertyId)).and(stayExpr))
				.where(lead.guestStatus.notEqualsIgnoreCase(CXL_RES).and(lead.guestStatus.notEqualsIgnoreCase(DNA_RES)))
				.transform(GroupBy.groupBy(lead.id)
						.as(Projections.bean(LeadContact.class, lead.id, lead.guestTypeCode, lead.leadStatusCode,
								lead.stayId, lead.tripTicketNumber, lead.customerUniqueId, lead.allocatedPersonId,
								lead.contactFlag, lead.invitedFlag, lead.arrivalDate, lead.departureDate, lead.roomName,
								lead.roomNumber, Projections.bean(Guest.class, guestProjections).as("guest"),
								Projections.bean(Stay.class, stayProjections).as("stay"))));
		return new ArrayList<>(transform.values());

	}

	@Override
	@Transactional
	public List<LeadContact> deallocateLeads(List<LeadContact> leads) {

		leads.forEach((lead) -> {
			lead.setRemarksText(CoreConstants.DEALLOCATED, FALSE);
			lead.setAllocatedPersonId(null);
			lead.setAllocatedPerson(null);
		});

		String strQuery = "UPDATE LeadContact SET " + "LeadContact.AllocatedPersonId= NULL , " // 1
				+ "LeadContact.RemarksText=? " // 2
				+ "WHERE LeadContact.OrganizationId=? " // 3
				+ "AND LeadContact.PropertyId =? " // 4
				+ "AND LeadContact.ReservationConfirmationNum = ? "// 5
				+ "AND LeadContact.RoomSequence=? " // 6
				+ "AND LeadContact.GuestSequence=?"; // 7

		Connection connection = getEntityManager().unwrap(SessionImpl.class).connection();
		PreparedStatement preparedStatement = null;
		try {
			connection.setAutoCommit(false);
			preparedStatement = connection.prepareStatement(strQuery);
			for (LeadContact lc : leads) {
				getEntityManager().detach(lc);
				preparedStatement.setString(1, lc.getRemarksText());
				preparedStatement.setString(2, lc.getId().getOrganizationId());
				preparedStatement.setInt(3, lc.getId().getPropertyId());
				preparedStatement.setString(4, lc.getId().getReservationConfirmationNum());
				preparedStatement.setInt(5, lc.getId().getRoomSequence());
				preparedStatement.setInt(6, lc.getId().getGuestSequence());
				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (LeadContact leadContact : leads) {
			getEntityManager().detach(leadContact);
		}
		Set<LeadContact> set = IterableHelper.convertSet(leads);
		Set<LeadContactHistory> histories = set.parallelStream().map(one -> new LeadContactHistory(one))
				.collect(Collectors.toSet());
		try {
			leadContactHistoryService.saveRaw(histories);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return leads;
	}

	private void doLeadStatusUpdate(LeadContact lead, Tour tour, String leadStatusCode, String remarksText,
			Integer propertyId, Integer userId) throws Exception {

		Property property = propertyService.findById(new PropertyId(tour.getId().getOrganizationId(), propertyId));
		if (lead != null) {
			lead.setTripTicketNumber(tour.getId().getTripTicketNumber());
			if (leadStatusCode != null) {
				lead.setLeadStatusCode(leadStatusCode);
			}
			if (remarksText != null) {
				lead.setRemarksText(remarksText + tour.getId().getTripTicketNumber(), FALSE);
			}
			lead.setUpdateDtm(new Date());
			lead.setShowDate(tour.getShowDate());
			lead.setUpdatePersonId(CoreConstants.SYSTEM_GENERATED);
			lead.setProperty(property);
			if (property != null) {
				lead.setInternalPropertyId(property.getInternalPropertyId());
			}
			if (CoreConstants.LEAD_STATUS_CODE_BK.equals(leadStatusCode)) {
				lead.setContactFlag(true);
				lead.setInvitedFlag(true);
			}
			if(userId!=null){
				lead = updateLeadContact(lead, leadStatusCode, userId);
			}
			leadContactService.save(lead, false);
		}
	}

	@Override
	public List<LeadContact> findAllocatable(List<GuestId> guestIds) {

		QLeadContact lead = new QLeadContact("LEAD");
		QStay stay = new QStay("STAY");
		QGuest guest = new QGuest("GUEST");

		JPAQuery<LeadContact> query = new JPAQuery<LeadContact>(getEntityManager()).from(lead).leftJoin(lead.stay, stay)
				.fetchJoin().leftJoin(lead.guest, guest).fetchJoin().where(lead.id.in(guestIds))
				.orderBy(guest.lastName.asc(), guest.firstName.asc(), lead.arrivalDate.asc());

		return query.fetch();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<LeadContact> findAllocatable(String organizationId, Integer propertyId, AllocationSearchDto dto) {

		// http://stackoverflow.com/questions/33959641/querydsl-null-entity-from-onetomany-association-with-projection-bean
		// used since QueryDsl cannot handle multiple nested GroupBy.set
		String strQuery = "SELECT lc FROM LeadContact lc LEFT JOIN FETCH lc.stay st "
				+ "LEFT JOIN FETCH lc.allocatedPerson ap LEFT JOIN FETCH lc.guest gu";

		strQuery += " WHERE lc.id.organizationId = :org AND lc.id.propertyId = :prop "
				+ " AND lc.arrivalDate BETWEEN :start AND :end " + " AND lower(lc.guestStatus) <> :status "
				+ " AND lc.activeFlag = :flag ";
		if (dto.isGuestTypes()) {
			strQuery += " AND lc.guestTypeCode IN :guestTypes ";
		}
		if (dto.isLeadStatuses()) {
			strQuery += " AND lc.leadStatusCode IN :leadStatuses ";
		}
		// strQuery += " ORDER BY gt.lastName, gt.firstName, lc.arrivalDate ";
		Query query = getEntityManager().createQuery(strQuery);

		Date start = java.sql.Date.valueOf(dto.getArrival());
		Date end = java.sql.Date.valueOf(dto.getDeparture());
		query.setParameter("org", organizationId);
		query.setParameter("prop", propertyId);
		query.setParameter("start", start);
		query.setParameter("end", end);
		query.setParameter("status", "cxl");
		query.setParameter("flag", true);

		if (dto.isGuestTypes()) {
			query.setParameter("guestTypes", dto.getGuestTypes());
		}
		if (dto.isLeadStatuses()) {
			query.setParameter("leadStatuses", dto.getLeadStatuses());
		}
		return query.getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<LeadContact> findByArrivalDeparture(String organizationId, Integer propertyId, Date start, Date end,
			Boolean isDeparture) {
		if (isDeparture == null) {
			isDeparture = false;
		}
		QStay stay = new QStay("STAY");
		QLeadContact lead = new QLeadContact("LEAD");
		BooleanExpression stayExpr = isDeparture ? lead.departureDate.between(start, end)
				: lead.arrivalDate.between(start, end);
		QPerson allocatedPerson = new QPerson("ALLOCATED_PERSON");
		QGuest guest = new QGuest("GUEST");

		QTour tour = new QTour("TOUR");

		Expression<?>[] guestProjections = { guest.firstName, guest.lastName, guest.guestStatus, guest.state,
				guest.arrivalTime, guest.spgTier, guest.pmsConfirmationNum, guest.addressLine1, guest.addressLine2,
				guest.addressLine3, guest.country, guest.zipCode, guest.emailAddress, guest.city, guest.middleInitial,
				guest.phoneNumber };
		Expression<?>[] stayProjections = { stay.stayLengthOfStay, stay.stayRoomRate, stay.stayRoomNumber,
				stay.stayRateCode };
		Expression<?>[] tourProjections = { tour.id.tripTicketNumber, tour.tourDate, tour.showDate,
				tour.tourManifestCode };

		// http://stackoverflow.com/questions/33959641/querydsl-null-entity-from-onetomany-association-with-projection-bean
		Map<GuestId, LeadContact> transform = new JPAQuery<LeadContact>(getEntityManager()).from(lead)
				.leftJoin(lead.guest, guest).leftJoin(lead.stay, stay).leftJoin(lead.allocatedPerson, allocatedPerson)
				.leftJoin(lead.tour, tour)
				.where(lead.id.organizationId.eq(organizationId).and(lead.id.propertyId.eq(propertyId)).and(stayExpr))
				.where(lead.guestStatus.notEqualsIgnoreCase(CXL_RES).and(lead.guestStatus.notEqualsIgnoreCase(DNA_RES)))
				.where(lead.activeFlag.isTrue()).orderBy(guest.lastName.asc())
				.transform(GroupBy.groupBy(lead.id)
						.as(Projections.bean(LeadContact.class, lead.id, lead.guestTypeCode, lead.leadStatusCode,
								lead.stayId, lead.tripTicketNumber, lead.customerUniqueId, lead.allocatedPersonId,
								lead.contactFlag, lead.invitedFlag, lead.arrivalDate, lead.departureDate, lead.language,
								lead.activatorId, lead.roomNumber, lead.guestStatus, lead.internalPropertyId,
								lead.contactTypeCode, lead.onsiteMarketingLocationId, lead.initialAllocatedPersonId,
								lead.conciergeSequenceId, lead.initialContactedPersonId, lead.seed, lead.optInFlag,
								lead.optInLastRefreshDtm, lead.bookingDtm, lead.roomName, lead.overrideGuestTypeFlag,
								lead.overrideLeadStatusFlag, lead.showOnTripTicketFlag, lead.showDate,
								lead.enhancedFlag, lead.systemFlag, lead.activeFlag, lead.odsUpdateDtm,
								lead.odsPackageId, lead.createPersonId, lead.promiseGiftFlag, lead.issueGiftFlag,
								lead.rateCode, Projections.bean(Guest.class, guestProjections).as("guest"),
								Projections.bean(Stay.class, stayProjections).as("stay"),
								Projections.bean(Tour.class, tourProjections).as("tour"))));
		return new ArrayList<>(transform.values());
	}

	@Override
	public List<LeadContact> findByArrivalDeparture(String organizationId, Integer propertyId, Date start, Date end,
			Integer personId, Boolean isDeparture) {
		if (isDeparture == null) {
			isDeparture = false;
		}
		QStay stay = new QStay("STAY");
		QLeadContact lead = new QLeadContact("LEAD");
		BooleanExpression stayExpr = isDeparture ? lead.departureDate.between(start, end)
				: lead.arrivalDate.between(start, end);
		stayExpr = stayExpr.and(lead.allocatedPersonId.eq(personId));
		QPerson allocatedPerson = new QPerson("ALLOCATED_PERSON");
		QGuest guest = new QGuest("GUEST");
		QTour tour = new QTour("TOUR");

		Expression<?>[] guestProjections = { guest.firstName, guest.lastName, guest.guestStatus, guest.state,
				guest.arrivalTime, guest.spgTier, guest.pmsConfirmationNum, guest.addressLine1, guest.addressLine2,
				guest.addressLine3, guest.country, guest.zipCode, guest.emailAddress, guest.city, guest.middleInitial,
				guest.phoneNumber };
		Expression<?>[] stayProjections = { stay.stayLengthOfStay, stay.stayRoomRate, stay.stayRoomNumber,
				stay.stayRateCode };
		Expression<?>[] tourProjections = { tour.id.tripTicketNumber, tour.tourDate, tour.showDate,
				tour.tourManifestCode };

		// http://stackoverflow.com/questions/33959641/querydsl-null-entity-from-onetomany-association-with-projection-bean
		Map<GuestId, LeadContact> transform = new JPAQuery<LeadContact>(getEntityManager()).from(lead)
				.leftJoin(lead.guest, guest).leftJoin(lead.stay, stay).leftJoin(lead.allocatedPerson, allocatedPerson)
				.leftJoin(lead.tour, tour)
				.where(lead.id.organizationId.eq(organizationId).and(lead.id.propertyId.eq(propertyId)).and(stayExpr))
				.where(lead.guestStatus.notEqualsIgnoreCase(CXL_RES).and(lead.guestStatus.notEqualsIgnoreCase(DNA_RES)))
				.where(lead.activeFlag.isTrue()).orderBy(guest.lastName.asc())
				.transform(GroupBy.groupBy(lead.id)
						.as(Projections.bean(LeadContact.class, lead.id, lead.guestTypeCode, lead.leadStatusCode,
								lead.stayId, lead.tripTicketNumber, lead.customerUniqueId, lead.allocatedPersonId,
								lead.contactFlag, lead.invitedFlag, lead.arrivalDate, lead.departureDate, lead.language,
								lead.activatorId, lead.roomNumber, lead.guestStatus, lead.internalPropertyId,
								lead.contactTypeCode, lead.onsiteMarketingLocationId, lead.initialAllocatedPersonId,
								lead.conciergeSequenceId, lead.initialContactedPersonId, lead.seed, lead.optInFlag,
								lead.optInLastRefreshDtm, lead.bookingDtm, lead.roomName, lead.overrideGuestTypeFlag,
								lead.overrideLeadStatusFlag, lead.showOnTripTicketFlag, lead.showDate,
								lead.enhancedFlag, lead.systemFlag, lead.activeFlag, lead.odsUpdateDtm,
								lead.odsPackageId, lead.createPersonId, lead.promiseGiftFlag, lead.issueGiftFlag,
								lead.rateCode, Projections.bean(Guest.class, guestProjections).as("guest"),
								Projections.bean(Stay.class, stayProjections).as("stay"),
								Projections.bean(Tour.class, tourProjections).as("tour"))));
		return new ArrayList<>(transform.values());
	}

	@Override
	public List<LeadContact> findByCustomerUniqueId(String organizationId, Integer propertyId,
			Integer customerUniqueId) {

		QLeadContact lead = new QLeadContact("LEAD");
		return getQueryFactory().selectFrom(lead)
				.where(lead.id.organizationId.eq(organizationId)
						.and(lead.id.propertyId.eq(propertyId).and(lead.customerUniqueId.eq(customerUniqueId)))
						.and(lead.guestStatus.notEqualsIgnoreCase(CXL_RES)))
				.fetch();

	}

	@Override
	public LeadContact findByIdWithStay(GuestId id) {

		QLeadContact lead = new QLeadContact("LEAD");
		QStay stay = new QStay("STAY");

		JPAQuery<LeadContact> query = new JPAQuery<LeadContact>(getEntityManager()).from(lead)
				.innerJoin(lead.stay, stay).where(lead.id.eq(id));

		return query.fetchOne();
	}

	@Override
	public LeadContact findByIdWithTripTicketNumber(String internalPropertyId, String tripTicketNumber) {

		QLeadContact lead = new QLeadContact("LEAD");

		JPAQuery<LeadContact> query = new JPAQuery<LeadContact>(getEntityManager()).from(lead)
				.where(lead.tripTicketNumber.eq(tripTicketNumber).and(lead.internalPropertyId.eq(internalPropertyId)));

		return query.fetchOne();
	}

	@Override
	public LeadContact findByResDetails(String resConfirmation, Integer roomSequence, Integer guestSequence) {

		QLeadContact lead = new QLeadContact("LEAD");
		QProperty property = new QProperty("PROPERTY");
		QGuest guest = new QGuest("GUEST");
		QStay stay = new QStay("STAY");
		JPAQuery<LeadContact> query = new JPAQuery<LeadContact>(getEntityManager()).from(lead)
				.leftJoin(lead.property, property).fetchJoin().leftJoin(lead.guest, guest).fetchJoin()
				.leftJoin(lead.stay, stay).fetchJoin().where(lead.id.reservationConfirmationNum.eq(resConfirmation)
						.and(lead.id.guestSequence.eq(guestSequence)).and(lead.id.roomSequence.eq(roomSequence)));

		return query.fetchOne();
	}

	@Override
	public List<LeadContact> findByResNum(String organizationId, Integer propertyId, String resNumber) {

		QLeadContact lead = new QLeadContact("LEAD");
		return getQueryFactory().selectFrom(lead)
				.where(lead.id.organizationId.eq(organizationId)
						.and(lead.id.propertyId.eq(propertyId).and(lead.id.reservationConfirmationNum.eq(resNumber))
								.and(lead.guestStatus.notEqualsIgnoreCase(CXL_RES))))
				.fetch();

	}

	@Override
	public List<LeadContact> findByResNumOrCUI(String organizationId, Integer propertyId, String resNumber,
			Integer customerUniqueId) {

		QLeadContact lead = new QLeadContact("LEAD");
		return getQueryFactory().selectFrom(lead)
				.where(lead.id.organizationId.like(organizationId)
						.and(lead.id.propertyId.eq(propertyId).and(lead.id.reservationConfirmationNum.eq(resNumber))
								.or(lead.customerUniqueId.eq(customerUniqueId))))
				.orderBy(lead.arrivalDate.asc(), lead.departureDate.asc()).fetch();
	}

	@Override
	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	public LeadContact findFullyLoadedWithoutHistories(GuestId id) {

		// http://stackoverflow.com/questions/33959641/querydsl-null-entity-from-onetomany-association-with-projection-bean
		// used since QueryDsl cannot handle multiple nested GroupBy.set
		Query query = getEntityManager().createQuery("SELECT lc FROM LeadContact lc LEFT JOIN FETCH lc.guest lcgt "
				+ " LEFT JOIN FETCH lc.stay lcst LEFT JOIN FETCH lc.leadStatus lcst "
				+ " LEFT JOIN FETCH lc.property lcprop LEFT JOIN FETCH lc.leadGifts lcgs "
				+ " LEFT JOIN FETCH lc.owners lcos "
				+ " LEFT JOIN FETCH lcos.ownerContracts lcoscont LEFT JOIN FETCH lcoscont.ownerInventories lcoscontinv "
				+ " LEFT JOIN FETCH lc.packages lcpk LEFT JOIN FETCH lcpk.packageGifts lcpkgs "
				+ " LEFT JOIN FETCH lc.tour lctour LEFT JOIN FETCH lctour.tourGifts lctourgs "
				+ " WHERE lc.id.organizationId = :org AND lc.id.propertyId = :prop AND lc.id.reservationConfirmationNum = :res  "
				+ " AND lc.id.roomSequence = :room AND lc.id.guestSequence = :guest");
		query.setParameter("org", id.getOrganizationId());
		query.setParameter("prop", id.getPropertyId());
		query.setParameter("res", id.getReservationConfirmationNum());
		query.setParameter("room", id.getRoomSequence());
		query.setParameter("guest", id.getGuestSequence());
		LeadContact found = (LeadContact) query.getSingleResult();
		if (found != null) {
			if (found.getLeadStatus() != null && found.getLeadStatus().getId() == null) {
				found.setLeadStatus(null);
			}
			if (found.getStayId() == null) {
				found.setStay(null);
			}
			if (found.getTour() != null && found.getTour().getId() == null) {
				found.setTour(null);
			}
			if (found.getLeadGifts() != null) {
				Set<LeadGift> loaded = new HashSet<>();
				found.getLeadGifts().forEach(one -> {
					if (one.getId() != null) {
						loaded.add(one);
					}
				});
				found.setLeadGifts(loaded);
			}
			if (found.getOwners() != null) {
				Set<Owner> owners = new HashSet<>();
				found.getOwners().forEach(owner -> {
					if (owner.getId() != null) {
						owners.add(owner);

						if (owner.getOwnerContracts() != null) {
							Set<OwnerContract> contracts = new HashSet<>();
							owner.getOwnerContracts().forEach(contract -> {
								if (contract.getId() != null) {
									contracts.add(contract);

									if (contract.getOwnerInventories() != null) {
										Set<OwnerInventory> inventories = new HashSet<>();
										contract.getOwnerInventories().forEach(inventory -> {
											if (inventory.getId() != null) {
												inventories.add(inventory);
											}
										});
										contract.setOwnerInventories(inventories);
									}
								}
							});
							owner.setOwnerContracts(contracts);
						}
					}
				});
				found.setOwners(owners);
			}
			if (found.getPackages() != null) {
				Set<Package> packages = new HashSet<>();
				found.getPackages().forEach(package_ -> {
					if (package_.getId() != null) {
						packages.add(package_);

						if (package_.getPackageGifts() != null) {
							Set<PackageGift> gifts = new HashSet<>();
							package_.getPackageGifts().forEach(gift -> {
								if (gift.getId() != null) {
									gifts.add(gift);
								}
							});
							package_.setPackageGifts(gifts);
						}
					}
				});
				found.setPackages(packages);
			}
			Set<Owner> recordSet = found.getOwners();
			if (recordSet != null) {
				Set<Owner> owners = new HashSet<>();
				found.getOwners().forEach(owner -> {
					if (owner.getId() != null) {
						owners.add(owner);
					}
				});
				found.setOwners(owners);
			}

			if (found.getTours() != null) {

				List<Tour> tours = new ArrayList<Tour>();
				Set<TourGift> tourGifts = new HashSet<>();
				found.getTours().forEach(tour -> {
					tourGifts.addAll(tour.getTourGifts());
					tours.add(tour);
				});
				found.setTours(tours);
			}

			if (found.getLinkedTours() != null) {
				List<Tour> tours = new ArrayList<Tour>();
				tours.addAll(found.getLinkedTours());
				found.setLinkedTours(tours);
			}
		}
		return found;
	}

	@Override
	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	public LeadContact findFullyLoaded(GuestId id) {

		// http://stackoverflow.com/questions/33959641/querydsl-null-entity-from-onetomany-association-with-projection-bean
		// used since QueryDsl cannot handle multiple nested GroupBy.set
		Query query = getEntityManager().createQuery("SELECT lc FROM LeadContact lc LEFT JOIN FETCH lc.guest lcgt "
				+ " LEFT JOIN FETCH lc.stay lcst LEFT JOIN FETCH lc.leadStatus lcst "
				+ " LEFT JOIN FETCH lc.property lcprop LEFT JOIN FETCH lc.leadGifts lcgs "
				+ " LEFT JOIN FETCH lc.owners lcos "
				+ " LEFT JOIN FETCH lcos.ownerContracts lcoscont LEFT JOIN FETCH lcoscont.ownerInventories lcoscontinv "
				+ " LEFT JOIN FETCH lc.packages lcpk LEFT JOIN FETCH lcpk.packageGifts lcpkgs "
				+ " LEFT JOIN FETCH lc.tour lctour LEFT JOIN FETCH lctour.tourGifts lctourgs "
				+ " LEFT JOIN FETCH lc.leadContactHistory lchist LEFT JOIN FETCH lchist.leadStatus lchistst"
				+ " LEFT JOIN FETCH lchist.allocatedPerson lchistall LEFT JOIN FETCH lchist.initialAllocatedPerson lchistinit "
				+ " LEFT JOIN FETCH lchist.createPerson lchistcrea LEFT JOIN FETCH lchist.initialContactedPerson lchistcinitrea "
				+ " LEFT JOIN FETCH lchist.activator lchistat "
				+ " WHERE lc.id.organizationId = :org AND lc.id.propertyId = :prop AND lc.id.reservationConfirmationNum = :res  "
				+ " AND lc.id.roomSequence = :room AND lc.id.guestSequence = :guest");
		query.setParameter("org", id.getOrganizationId());
		query.setParameter("prop", id.getPropertyId());
		query.setParameter("res", id.getReservationConfirmationNum());
		query.setParameter("room", id.getRoomSequence());
		query.setParameter("guest", id.getGuestSequence());
		LeadContact found = (LeadContact) query.getSingleResult();
		if (found != null) {
			if (found.getLeadStatus() != null && found.getLeadStatus().getId() == null) {
				found.setLeadStatus(null);
			}
			if (found.getStayId() == null) {
				found.setStay(null);
			}
			if (found.getTour() != null && found.getTour().getId() == null) {
				found.setTour(null);
			}
			if (found.getLeadContactHistory() != null) {
				Set<LeadContactHistory> loaded = new HashSet<>();
				found.getLeadContactHistory().forEach(one -> {
					if (one.getId() != null) {
						loaded.add(one);
					}
					if (one.getActivator() != null && one.getActivator().getId() == null) {
						one.setActivator(null);
					}
					if (one.getAllocatedPersonId() == null) {
						one.setAllocatedPerson(null);
					}
					if (one.getInitialContactedPersonId() == null) {
						one.setInitialContactedPerson(null);
					}
					if (one.getInitialAllocatedPersonId() == null) {
						one.setInitialAllocatedPerson(null);
					}
					if (one.getCreatePersonId() == null) {
						one.setCreatePerson(null);
					}
				});
				found.setLeadContactHistory(loaded);
			}
			if (found.getLeadGifts() != null) {
				Set<LeadGift> loaded = new HashSet<>();
				found.getLeadGifts().forEach(one -> {
					if (one.getId() != null) {
						loaded.add(one);
					}
				});
				found.setLeadGifts(loaded);
			}
			if (found.getOwners() != null) {
				Set<Owner> owners = new HashSet<>();
				found.getOwners().forEach(owner -> {
					if (owner.getId() != null) {
						owners.add(owner);

						if (owner.getOwnerContracts() != null) {
							Set<OwnerContract> contracts = new HashSet<>();
							owner.getOwnerContracts().forEach(contract -> {
								if (contract.getId() != null) {
									contracts.add(contract);

									if (contract.getOwnerInventories() != null) {
										Set<OwnerInventory> inventories = new HashSet<>();
										contract.getOwnerInventories().forEach(inventory -> {
											if (inventory.getId() != null) {
												inventories.add(inventory);
											}
										});
										contract.setOwnerInventories(inventories);
									}
								}
							});
							owner.setOwnerContracts(contracts);
						}
					}
				});
				found.setOwners(owners);
			}
			if (found.getPackages() != null) {
				Set<Package> packages = new HashSet<>();
				found.getPackages().forEach(package_ -> {
					if (package_.getId() != null) {
						packages.add(package_);

						if (package_.getPackageGifts() != null) {
							Set<PackageGift> gifts = new HashSet<>();
							package_.getPackageGifts().forEach(gift -> {
								if (gift.getId() != null) {
									gifts.add(gift);
								}
							});
							package_.setPackageGifts(gifts);
						}
					}
				});
				found.setPackages(packages);
			}
			Set<Owner> recordSet = found.getOwners();
			if (recordSet != null) {
				Set<Owner> owners = new HashSet<>();
				found.getOwners().forEach(owner -> {
					if (owner.getId() != null) {
						owners.add(owner);
					}
				});
				found.setOwners(owners);
			}

			if (found.getTours() != null) {

				List<Tour> tours = new ArrayList<Tour>();
				Set<TourGift> tourGifts = new HashSet<>();
				found.getTours().forEach(tour -> {
					tourGifts.addAll(tour.getTourGifts());
					tours.add(tour);
				});
				found.setTours(tours);
			}

			if (found.getLinkedTours() != null) {
				List<Tour> tours = new ArrayList<Tour>();
				tours.addAll(found.getLinkedTours());
				found.setLinkedTours(tours);
			}
		}
		return found;
	}

	@Override
	public List<LeadContact> findFullyLoaded(String organizationId, Integer propertyId) {

		// stay has to overlap today
		QStay stay = new QStay("STAY");

		Date todayZero = DateHelper.dateZeroTime(getTZCorrectedDate(new Date(), propertyId, organizationId)); // FIXME

		QLeadContact lead = new QLeadContact("LEAD");
		BooleanExpression stayExpr = lead.arrivalDate.loe(todayZero).and(lead.departureDate.goe(todayZero))
				.and(lead.guestStatus.notEqualsIgnoreCase(CXL_RES).and(lead.guestStatus.notEqualsIgnoreCase(COUT_RES))
						.and(lead.guestStatus.notEqualsIgnoreCase(DNA_RES)));

		QPerson allocatedPerson = new QPerson("ALLOCATED_PERSON");
		QGuest guest = new QGuest("GUEST");
		QTour tour = new QTour("TOUR");

		Expression<?>[] guestProjections = { guest.firstName, guest.lastName, guest.guestStatus, guest.state,
				guest.arrivalTime, guest.spgTier, guest.pmsConfirmationNum, guest.addressLine1, guest.addressLine2,
				guest.addressLine3, guest.country, guest.zipCode, guest.emailAddress, guest.city, guest.middleInitial,
				guest.phoneNumber };
		Expression<?>[] stayProjections = { stay.stayLengthOfStay, stay.stayRoomRate, stay.stayRoomNumber,
				stay.stayRateCode };
		Expression<?>[] tourProjections = { tour.id.tripTicketNumber, tour.tourDate, tour.showDate,
				tour.tourManifestCode };

		// http://stackoverflow.com/questions/33959641/querydsl-null-entity-from-onetomany-association-with-projection-bean
		Map<GuestId, LeadContact> transform = new JPAQuery<LeadContact>(getEntityManager()).from(lead)
				.leftJoin(lead.guest, guest).leftJoin(lead.stay, stay).leftJoin(lead.allocatedPerson, allocatedPerson)
				.leftJoin(lead.tour, tour)
				.where(lead.id.organizationId.eq(organizationId).and(lead.id.propertyId.eq(propertyId)).and(stayExpr))
				.where(lead.guestStatus.notEqualsIgnoreCase(CXL_RES)).where(lead.activeFlag.isTrue())
				.orderBy(guest.lastName.asc())
				.transform(GroupBy.groupBy(lead.id)
						.as(Projections.bean(LeadContact.class, lead.id, lead.guestTypeCode, lead.leadStatusCode,
								lead.stayId, lead.tripTicketNumber, lead.customerUniqueId, lead.allocatedPersonId,
								lead.contactFlag, lead.invitedFlag, lead.arrivalDate, lead.departureDate, lead.language,
								lead.activatorId, lead.roomNumber, lead.guestStatus, lead.internalPropertyId,
								lead.contactTypeCode, lead.onsiteMarketingLocationId, lead.initialAllocatedPersonId,
								lead.conciergeSequenceId, lead.initialContactedPersonId, lead.seed, lead.optInFlag,
								lead.optInLastRefreshDtm, lead.bookingDtm, lead.roomName, lead.overrideGuestTypeFlag,
								lead.overrideLeadStatusFlag, lead.showOnTripTicketFlag, lead.showDate,
								lead.enhancedFlag, lead.systemFlag, lead.activeFlag, lead.odsUpdateDtm,
								lead.odsPackageId, lead.createPersonId, lead.promiseGiftFlag, lead.issueGiftFlag,
								Projections.bean(Guest.class, guestProjections).as("guest"),
								Projections.bean(Stay.class, stayProjections).as("stay"),
								Projections.bean(Tour.class, tourProjections).as("tour"))));
		return new ArrayList<>(transform.values());
	}

	@Override
	public List<LeadContact> findFullyLoaded(String organizationId, Integer propertyId, Integer personId) {

		// stay has to overlap today
		QStay stay = new QStay("STAY");

		Date todayZero = DateHelper.dateZeroTime(getTZCorrectedDate(new Date(), propertyId, organizationId));
		QLeadContact lead = new QLeadContact("LEAD");
		BooleanExpression stayExpr = lead.arrivalDate.loe(todayZero).and(lead.departureDate.goe(todayZero))
				.and(lead.guestStatus.notEqualsIgnoreCase(CXL_RES).and(lead.guestStatus.notEqualsIgnoreCase(DNA_RES)));
		if (personId != null) {
			// person needs to be (allocated or (contacted and not allocated))
			stayExpr = stayExpr.and(lead.allocatedPersonId.eq(personId));
		}

		QLeadStatus leadStatus = new QLeadStatus("LEADSTATUS");
		QPerson allocatedPerson = new QPerson("ALLOCATED_PERSON");
		QGuest guest = new QGuest("GUEST");
		QTour tour = new QTour("TOUR");

		Expression<?>[] guestProjections = { guest.firstName, guest.lastName, guest.guestStatus, guest.state,
				guest.arrivalTime, guest.spgTier, guest.pmsConfirmationNum, guest.addressLine1, guest.addressLine2,
				guest.addressLine3, guest.country, guest.zipCode, guest.emailAddress, guest.city, guest.middleInitial,
				guest.phoneNumber };
		Expression<?>[] stayProjections = { stay.stayLengthOfStay, stay.stayRoomRate, stay.stayRoomNumber,
				stay.stayRateCode };
		Expression<?>[] tourProjections = { tour.id.tripTicketNumber, tour.tourDate, tour.showDate,
				tour.tourManifestCode };

		// http://stackoverflow.com/questions/33959641/querydsl-null-entity-from-onetomany-association-with-projection-bean
		Map<GuestId, LeadContact> transform = new JPAQuery<LeadContact>(getEntityManager()).from(lead)
				.leftJoin(lead.guest, guest).leftJoin(lead.stay, stay).leftJoin(lead.leadStatus, leadStatus)
				.leftJoin(lead.allocatedPerson, allocatedPerson).leftJoin(lead.tour, tour)
				.where(lead.id.organizationId.eq(organizationId).and(lead.id.propertyId.eq(propertyId)).and(stayExpr))
				.transform(GroupBy.groupBy(lead.id)
						.as(Projections.bean(LeadContact.class, lead.id, lead.guestTypeCode, lead.leadStatusCode,
								lead.stayId, lead.tripTicketNumber, lead.customerUniqueId, lead.allocatedPersonId,
								lead.allocatedPerson, lead.contactFlag, lead.invitedFlag, lead.arrivalDate,
								lead.departureDate, lead.roomNumber,
								Projections.bean(Guest.class, guestProjections).as("guest"),
								Projections.bean(Stay.class, stayProjections).as("stay"),
								Projections.bean(Tour.class, tourProjections).as("tour"))));
		return new ArrayList<>(transform.values());
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<LeadContact> findLazily(List<GuestId> guestIds) {

		List<LeadContact> results = new ArrayList<>();
		for (GuestId guest : guestIds) {
			String sql = "SELECT * FROM LeadContact lc WHERE lc.organizationId =? " + "AND lc.propertyId =? "
					+ "AND lc.reservationConfirmationNum =? " + "AND lc.roomSequence=? " + "AND lc.guestSequence=?";
			LeadContact lc = (LeadContact) jdbcTemplate.queryForObject(sql,
					new Object[] { guest.getOrganizationId(), guest.getPropertyId(),
							guest.getReservationConfirmationNum(), guest.getRoomSequence(), guest.getGuestSequence() },
					new LeadContactRowMapper());
			results.add(lc);

		}
		return results;
	}

	@Override
	public List<LeadContact> findLeadContact(String organizationId, Integer propertyId, Integer personId) {

		QLeadContact lead = new QLeadContact("LEAD");
		Date today = DateHelper.dateZeroTime(getTZCorrectedDate(new Date(), propertyId, organizationId));
		JPAQuery<LeadContact> query = new JPAQuery<LeadContact>(getEntityManager()).from(lead)
				.where(lead.allocatedPersonId.eq(personId).and(lead.arrivalDate.loe(today)));
		return query.fetch();
	}

	@Override
	public List<LeadContact> findLeadContacts(String organizationId, Integer propertyId, Integer customerUniqueId) {

		QLeadContact lead = new QLeadContact("LEAD");
		QProperty property = new QProperty("PROPERTY");
		QGuest guest = new QGuest("GUEST");
		QLeadStatus leadStatus = new QLeadStatus("LEAD_STATUS");
		return getQueryFactory().selectFrom(lead).leftJoin(lead.property, property).fetchJoin()
				.leftJoin(lead.guest, guest).fetchJoin().leftJoin(lead.leadStatus, leadStatus).fetchJoin()
				.where(lead.id.organizationId.like(organizationId)
						.and(lead.id.propertyId.eq(propertyId).and(lead.customerUniqueId.eq(customerUniqueId))))
				.orderBy(lead.arrivalDate.asc(), lead.departureDate.asc()).fetch();

	}

	@Override
	public LeadContact findOneLead(String organizationId, Integer propertyId, String resNumber, Integer roomSequence,
			Integer guestSequence) {

		QLeadContact lead = new QLeadContact("LEAD");
		return getQueryFactory().selectFrom(lead)
				.where(lead.id.organizationId.eq(organizationId)
						.and(lead.id.propertyId.eq(propertyId).and(lead.id.reservationConfirmationNum.eq(resNumber).and(
								lead.id.roomSequence.eq(roomSequence).and(lead.id.guestSequence.eq(guestSequence))))))
				.fetchOne();
	}

	@Override
	public LeadContact getLeadContacts(String organizationId, Integer propertyId, Integer customerUniqueId) {

		QLeadContact lead = new QLeadContact("LEAD");
		QProperty property = new QProperty("PROPERTY");
		QGuest guest = new QGuest("GUEST");
		QLeadStatus leadStatus = new QLeadStatus("LEAD_STATUS");
		return getQueryFactory().selectFrom(lead).leftJoin(lead.property, property).fetchJoin()
				.leftJoin(lead.guest, guest).fetchJoin().leftJoin(lead.leadStatus, leadStatus).fetchJoin()
				.where(lead.id.organizationId.like(organizationId)
						.and(lead.id.propertyId.eq(propertyId).and(lead.customerUniqueId.eq(customerUniqueId))))
				.fetchOne();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LeadContact> getLeadContactsForEmailPreferenceTier1(String organizationId, Integer propertyId) {

		ZonedDateTime date = ZonedDateTime.now().minusHours(12);
		ZonedDateTime daysPast = ZonedDateTime.now().minusDays(7);

		String strQuery = "SELECT lc FROM LeadContact lc LEFT JOIN FETCH lc.guest gu LEFT JOIN FETCH lc.stay st "
				+ " WHERE lc.id.organizationId = :org " + "AND lc.id.propertyId = :prop "
				+ " AND lower(lc.guestStatus) <> :status " + " AND lc.optInFlag =:flag "
				+ " AND lc.optInLastRefreshDtm < :date " + " AND lc.departureDate > :daysPast ";

		Query query = getEntityManager().createQuery(strQuery);

		query.setParameter("org", organizationId);
		query.setParameter("prop", propertyId);
		query.setParameter("status", "cxl");
		query.setParameter("flag", true);
		query.setParameter("date", Date.from(date.toInstant()));
		query.setParameter("daysPast", Date.from(daysPast.toInstant()));

		return query.getResultList();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LeadContact> getLeadContactsForEmailPreferenceTier2(String organizationId, Integer propertyId) {

		String strQuery = "SELECT lc FROM LeadContact lc LEFT JOIN FETCH lc.guest gu LEFT JOIN FETCH lc.stay st "
				+ " WHERE lc.id.organizationId = :org " + " AND lc.id.propertyId = :prop "
				+ " AND gu.emailAddress IS NOT NULL " + " AND lc.optInLastRefreshDtm IS NULL "
				+ " AND lc.optInFlag = :optInFlag " + " AND lc.id.guestSequence = :sequence "
				+ " AND gu.primaryFlag = :primaryFlag ";

		Query query = getEntityManager().createQuery(strQuery);

		query.setParameter("org", organizationId);
		query.setParameter("prop", propertyId);
		query.setParameter("optInFlag", false);
		query.setParameter("sequence", 1);
		query.setParameter("primaryFlag", true);

		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LeadContact> getLeadsToRederive(String organizationId, Integer propertyId, Date start, Date end) {

		Query query = getEntityManager().createQuery("SELECT lc FROM LeadContact lc LEFT JOIN FETCH lc.stay st "
				+ " LEFT JOIN FETCH lc.owners os LEFT JOIN FETCH os.ownerContracts ocs "
				+ " LEFT JOIN FETCH ocs.ownerInventories ois LEFT JOIN FETCH lc.packages ps "
				+ " WHERE lc.id.organizationId = :org AND lc.id.propertyId = :prop "
				+ " AND lc.arrivalDate BETWEEN :arrive AND :depart ");
		query.setParameter("org", organizationId);
		query.setParameter("prop", propertyId);
		query.setParameter("arrive", start);
		query.setParameter("depart", end);
		List<LeadContact> leads = query.getResultList();

		return leads;
	}

	@Override
	protected CrudRepository<LeadContact, GuestId> getRepository() {

		return leadContactRepository;
	}

	public Date getTZCorrectedDate(Date inDate, Integer propertyId, String organizationId) {
		Property property1 = propertyService.findById(new PropertyId(organizationId, propertyId));
		Timezone timezone = timezoneService.find(new TimezoneId(organizationId, property1.getTimezoneId()));
		Instant instant = inDate.toInstant();
		ZoneId zone = ZoneId.of(timezone.getTimezoneName());
		ZonedDateTime zonedDt = instant.atZone(zone);
		LocalDate localDate = zonedDt.toLocalDate();
		Date outDate = java.util.Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		return outDate;
	}

	public void linkTour(LeadContact leadContact, Tour tour) {

		if (tour != null && tour.getConfirmationNumber() != null) {
			try {
				if (leadContact != null) {
					leadContact.setTripTicketNumber(tour.getId().getTripTicketNumber());
					// leadContact.setLeadStatusCode(leadStatusCode); Logic to
					// determine lead status
					// code.
					leadContact.setRemarksText("Tour link: " + tour.getId().getTripTicketNumber(), FALSE);
					leadContact.setUpdateDtm(new Date());
					leadContact.setShowDate(tour.getShowDate());
					leadContact.setUpdatePersonId(CoreConstants.SYSTEM_GENERATED);
					// leadContact.setProperty(property);
					leadContactService.save(leadContact, false);
				}
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Exception during lead contact update", e);

			}
		} else {
			LOGGER.warning("Attempted to update a lead status without correct information");
		}

	}

	@Override
	public Iterable<LeadContact> nonTransSave(List<LeadContact> lead) throws SQLException {

		Set<LeadContact> set = IterableHelper.convertSet(lead);
		Set<LeadContactHistory> histories = set.stream().map(one -> new LeadContactHistory(one))
				.collect(Collectors.toSet());
		try {
			leadContactHistoryService.saveRaw(histories);
		} catch (SQLException e) {
			throw e;
		}
		return this.leadContactRepository.save(lead);
	}

	@Override
	@Transactional
	public Set<LeadContact> save(Iterable<LeadContact> entities) {

		// Used Hibernate Interceptor but got duplicates
		// Fix would be to use Thread Local
		// http://stackoverflow.com/questions/22931751/hibernate-interceptor-onflushdirty-called-multiple-times-with-same-values
		// Another option to use Hibernate Listener
		Set<LeadContact> set = IterableHelper.convertSet(entities);
		Set<LeadContactHistory> histories = set.stream().map(one -> new LeadContactHistory(one))
				.collect(Collectors.toSet());
		leadContactHistoryService.save(histories);
		return super.save(entities);
	}

	@Override
	@Transactional
	public LeadContact save(LeadContact entity) {

		// Used Hibernate Interceptor but got duplicates
		// Fix would be to use Thread Local
		// http://stackoverflow.com/questions/22931751/hibernate-interceptor-onflushdirty-called-multiple-times-with-same-values
		// Another option to use Hibernate Listener
		LeadContactHistory history = new LeadContactHistory(entity);
		leadContactHistoryService.save(history);
		return super.save(entity);
	}

	@Override
	@Transactional
	public LeadContact save(LeadContact entity, Boolean isSystemFlag) {

		// Used Hibernate Interceptor but got duplicates
		// Fix would be to use Thread Local
		// http://stackoverflow.com/questions/22931751/hibernate-interceptor-onflushdirty-called-multiple-times-with-same-values
		// Another option to use Hibernate Listener
		LeadContactHistory history = new LeadContactHistory(entity);
		history.setSystemFlag(isSystemFlag);
		leadContactHistoryService.save(history);
		return super.save(entity);
	}

	@Transactional
	@Override
	public void saveNative(Iterable<LeadContact> entities) throws SQLException {

		Connection connection = getEntityManager().unwrap(SessionImpl.class).connection();
		try {
			String strQuery = "UPDATE LeadContact SET " + "LeadContact.GuestTypeCode=?, " // 1
					+ "LeadContact.AllocatedPersonId=?, " // 2
					+ "LeadContact.InitialAllocatedPersonId=?, " // 3
					+ "LeadContact.LeadStatusCode=?, " // 4
					+ "LeadContact.RemarksText=? " // 5
					+ "WHERE LeadContact.OrganizationId=? " // 6
					+ "AND LeadContact.PropertyId =? " // 7
					+ "AND LeadContact.ReservationConfirmationNum = ? "// 8
					+ "AND LeadContact.RoomSequence=? " // 9
					+ "AND LeadContact.GuestSequence=? " // 10
					+ "AND LeadContact.OverrideGuestTypeFlag=? " //11
					+  "AND LeadContact.OverrideLeadStatusFlag=?"; //12
			PreparedStatement preparedStatement = null;
			preparedStatement = connection.prepareStatement(strQuery);
			connection.setAutoCommit(false);
			for (LeadContact lc : entities) {
				getEntityManager().detach(lc);
				preparedStatement.setString(1, lc.getGuestTypeCode());
				if (lc.getAllocatedPersonId() != null) {
					preparedStatement.setInt(2, lc.getAllocatedPersonId());
				} else {
					preparedStatement.setNull(2, Types.INTEGER);
				}
				if (lc.getInitialAllocatedPersonId() != null) {
					preparedStatement.setInt(3, lc.getInitialAllocatedPersonId());
				} else {
					preparedStatement.setNull(3, Types.INTEGER);
				}
				preparedStatement.setString(4, lc.getLeadStatusCode());
				preparedStatement.setString(5, lc.getRemarksText());
				preparedStatement.setString(6, lc.getId().getOrganizationId());
				if (lc.getId().getOrganizationId() != null) {
					preparedStatement.setInt(7, lc.getId().getPropertyId());
				} else {
					preparedStatement.setNull(7, Types.INTEGER);
				}
				preparedStatement.setString(8, lc.getId().getReservationConfirmationNum());
				if (lc.getId().getRoomSequence() != null) {

					preparedStatement.setInt(9, lc.getId().getRoomSequence());
				} else {
					preparedStatement.setNull(9, Types.INTEGER);
				}

				if (lc.getId().getGuestSequence() != null) {

					preparedStatement.setInt(10, lc.getId().getGuestSequence());
				} else {
					preparedStatement.setNull(10, Types.INTEGER);
				}

				if (lc.getOverrideGuestTypeFlag() != null) {
					preparedStatement.setBoolean(11, lc.getOverrideGuestTypeFlag());
				} else {
					preparedStatement.setBoolean(11, false);
				}

				if (lc.getOverrideLeadStatusFlag() != null) {
					preparedStatement.setBoolean(12, lc.getOverrideLeadStatusFlag());
				} else {
					preparedStatement.setBoolean(12, false);
				}
				preparedStatement.addBatch();
			}
			StopWatch watch = new StopWatch();
			watch.start();
			preparedStatement.executeBatch();
			watch.stop();
			LOGGER.log(Level.INFO,
					"ASTIME Lead Contact Batch - Elapsed time in seconds: " + watch.getTotalTimeSeconds());
		} catch (Exception e) {
			LOGGER.log(Level.INFO, "Problem Saving Allocations", e.getMessage());
			throw e;
		}
		Set<LeadContact> set = IterableHelper.convertSet(entities);
		Set<LeadContactHistory> histories = set.stream().map(one -> new LeadContactHistory(one))
				.collect(Collectors.toSet());
		try {
			leadContactHistoryService.saveRaw(histories);
		} catch (SQLException e) {
			LOGGER.log(Level.INFO, "Problem Saving Lead Contact Histories", e.getMessage());
			throw e;
		}
		// connection.commit();
	}

	@Override
	@Transactional(readOnly = true)
	public List<LeadContactDto> searchByGuest(GuestSearchDto guestSearchDto, Integer internalProperty,
			String organization) {
		List<Lookup> guestTypes = lookupService.getLookUpData(organization, LookupService.GUEST_TYPE, internalProperty);

		List<Person> persons = personService.findByOrganizationAndProperty(organization, internalProperty);
		Map<Integer, Person> mapPersons = persons.stream().collect(Collectors.toMap(Person::getId, p -> p));
		QGuest qGuest = new QGuest("GUEST");
		QPerson qPerson = new QPerson("PERSON");
		QLeadContact qLeadContact = new QLeadContact("LEADCONTACT");
		BooleanBuilder tourSearchBuilder = new BooleanBuilder();

		if (guestSearchDto.getArrivalDateStart() != null && guestSearchDto.getArrivalDateEnd() != null) {
			Date start = Date
					.from(guestSearchDto.getArrivalDateStart().atStartOfDay(ZoneId.systemDefault()).toInstant());
			Date end = Date.from(guestSearchDto.getArrivalDateEnd().atStartOfDay(ZoneId.systemDefault()).toInstant());
			tourSearchBuilder.and(qLeadContact.arrivalDate.between(start, end));
		}

		if (!StringUtils.isBlank(guestSearchDto.getFirstName())) {
			tourSearchBuilder.and(qLeadContact.guest.firstName.equalsIgnoreCase(guestSearchDto.getFirstName()));
		}

		if (!StringUtils.isBlank(guestSearchDto.getLastName())) {
			tourSearchBuilder.and(qLeadContact.guest.lastName.equalsIgnoreCase(guestSearchDto.getLastName()));
		}

		if (!StringUtils.isBlank(guestSearchDto.getPmsConfirmation())) {
			tourSearchBuilder.and(qLeadContact.guest.pmsConfirmationNum.like(guestSearchDto.getPmsConfirmation()));
		}
		if (!StringUtils.isBlank(guestSearchDto.getReservationConfirmation())) {
			tourSearchBuilder
					.and(qLeadContact.id.reservationConfirmationNum.like(guestSearchDto.getReservationConfirmation()));
		}

		tourSearchBuilder.and(qLeadContact.guest.id.organizationId.eq(organization))
				.and(qLeadContact.guest.id.propertyId.eq(internalProperty));

		return getQueryFactory().selectFrom(qLeadContact).leftJoin(qLeadContact.guest, qGuest)
				.leftJoin(qLeadContact.allocatedPerson, qPerson).where(tourSearchBuilder).fetch().stream().map(lead -> {
					lead.setGuestType(guestTypes);

					Person person = mapPersons.get(lead.getAllocatedPersonId());
					String name = "";
					if (person != null) {
						name = person.getFirstName() + StringUtils.SPACE + person.getLastName();
					}
					LeadContactDto dto = new LeadContactDto(lead);
					dto.setAssignedVSCName(name);
					return dto;
				}).collect(Collectors.toList());
	}

	@Autowired
	public void setDataSource(DataSource datasource) {

		this.jdbcTemplate = new JdbcTemplate(datasource);
	}

	@Override
	public String toString() {

		return super.toString();
	}

	@Override
	public LeadContact updateLeadContact(LeadContact lead, String leadStatusCode, Integer user) {

		LeadStatus status = leadStatusSerivce
				.find(new LeadStatusId(lead.getId().getOrganizationId(), lead.getId().getPropertyId(), leadStatusCode));
		if (status.getReassignedFlag()) {
			lead.setAllocatedPersonId(user);
			if (lead.getInitialAllocatedPersonId() == null) {
				lead.setInitialAllocatedPersonId(user);
			}
		}
		return lead;
	}

	@Override
	@Transactional
	public void updateLeadsForEmailoptin(List<LeadContact> leadHavingEmailPreferences) {

		List<Guest> guestsUpdated = new ArrayList<>();

		leadHavingEmailPreferences.forEach(lead -> {
			Guest guest = new Guest();
			BeanUtils.copyProperties(lead.getGuest(), guest);
			guestsUpdated.add(guest);
			lead.setLinkedTours(null);
			lead.setOwners(null);
			lead.setPackages(null);
		});
		try {
			if (!guestsUpdated.isEmpty()) {
				this.guestService.save(guestsUpdated);
			}

			if (!leadHavingEmailPreferences.isEmpty()) {
				this.leadContactService.save(leadHavingEmailPreferences);
			}
		} catch (Exception e) {
			String failed = "<------------- Email Preference : Failed updating leads ---------------> ";
			LOGGER.log(Level.SEVERE, failed, e);
		}

	}

	@Override
	@Transactional
	public void updateLeadStatus(String organization, Integer property, List<Tour> tours, String leadStatusCode) {

		AuditEventType type = auditEventTypeService.findByAuditEventTypeDesc(AUDIT_EVENT_TYPE_TOUR_SYNC);

		tours.forEach(tour -> {

			LeadContact lead = leadContactRepository.findByIdOrganizationIdAndInternalPropertyIdAndTripTicketNumber(
					organization, tour.getId().getInternalPropertyId(), tour.getId().getTripTicketNumber());

			if (lead != null) {

				lead.setLeadStatusCode(leadStatusCode);
				lead.setRemarksText(TOUR_SYNC_REMARKS_TEXT + tour.getId().getTripTicketNumber() + " (toursync)", TRUE);
				lead.setUpdateDtm(new Date());
				lead.setLinkedTours(null);
				lead.setOwners(null);
				lead.setPackages(null);
				leadContactRepository.save(lead);

				AuditEvent event = new AuditEvent(organization, property, type.getId(),
						String.format(TOUR_SYNC_UPDATE, tour.getId().getTripTicketNumber()));

				auditEventService.save(event);

			}

		});
	}

	@Override
	@Transactional
	public void updateLeadStatus(Tour tour, String leadStatusCode, String remarksText, GuestId guestId) {

		LeadContact lead = find(guestId);
		if (tour != null && guestId != null) {
			try {
				doLeadStatusUpdate(lead, tour, leadStatusCode, remarksText, guestId.getPropertyId(), null);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Exception during lead contact update", e);

			}
		} else {
			LOGGER.warning("Attempted to update a lead status without correct information");
		}
	}

	@Override
	@Transactional
	public void updateLeadStatus(Tour tour, String leadStatusCode, String remarksText, Integer roomSequence,
			Integer guestSequence, Integer propertyId, Integer userId) {

		if (tour != null && tour.getConfirmationNumber() != null && roomSequence != null && guestSequence != null) {
			try {
				LeadContact lead = findByResDetails(tour.getReservationConfirmationNum(), roomSequence, guestSequence);
				doLeadStatusUpdate(lead, tour, leadStatusCode, remarksText, propertyId, userId);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Exception during lead contact update", e);

			}
		} else {
			LOGGER.warning("Attempted to update a lead status without correct information");
		}

	}

	@Override
	@Transactional
	public void updateLeadStatus(Tour tour, String leadStatusCode, String remarksText, Integer roomSequence,
			Integer guestSequence, Integer propertyId) {

		updateLeadStatus(tour, leadStatusCode, remarksText, roomSequence, guestSequence, propertyId, null);
	}
}