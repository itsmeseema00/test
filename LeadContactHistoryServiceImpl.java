package com.vistana.onsiteconcierge.core.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.internal.SessionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.querydsl.jpa.impl.JPAQuery;
import com.vistana.onsiteconcierge.core.dao.LeadContactHistoryRepository;
import com.vistana.onsiteconcierge.core.model.GuestId;
import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.model.LeadContactHistory;
import com.vistana.onsiteconcierge.core.model.LeadContactHistoryId;
import com.vistana.onsiteconcierge.core.model.QActivator;
import com.vistana.onsiteconcierge.core.model.QLeadContactHistory;
import com.vistana.onsiteconcierge.core.model.QLeadStatus;
import com.vistana.onsiteconcierge.core.model.QPerson;
import com.vistana.onsiteconcierge.core.service.LeadContactHistoryService;
import com.vistana.util.IterableHelper;

@Service
public class LeadContactHistoryServiceImpl extends SaveDeleteServiceImpl<LeadContactHistory, LeadContactHistoryId>
		implements LeadContactHistoryService {

	private static final Logger LOGGER = Logger.getLogger(LeadContactHistoryServiceImpl.class.getName());
	@Autowired
	private LeadContactHistoryRepository repository;

	/**
	 * Convert a java.util date to a java.sql date.
	 *
	 * @param date
	 * @return
	 */
	private Timestamp convertSqlDate(Date date) {

		if (date == null) {
			return null;
		} else {
			return new Timestamp(date.getTime());
		}
	}

	@Override
	public List<LeadContactHistory> findAllComments(GuestId guestId) {

		QLeadContactHistory leadHistory = new QLeadContactHistory("LEAD");
		QLeadStatus leadStatus = new QLeadStatus("leadStatus");
		QPerson allocated = new QPerson("person1");
		QPerson initialAllocated = new QPerson("person2");
		QPerson initialContacted = new QPerson("person3");
		QPerson createPerson = new QPerson("person4");
		QActivator activator = new QActivator("activator");

		return getQueryFactory().selectFrom(leadHistory).leftJoin(leadHistory.leadStatus, leadStatus).fetchJoin()
				.leftJoin(leadHistory.allocatedPerson, allocated).fetchJoin()
				.leftJoin(leadHistory.initialAllocatedPerson, initialAllocated).fetchJoin()
				.leftJoin(leadHistory.initialContactedPerson, initialContacted).fetchJoin()
				.leftJoin(leadHistory.activator, activator).fetchJoin().leftJoin(leadHistory.createPerson, createPerson)
				.fetchJoin().orderBy(leadHistory.id.createDtm.desc())
				.where(leadHistory.id.organizationId.eq(guestId.getOrganizationId())
						.and(leadHistory.id.guestSequence.eq(guestId.getGuestSequence()))
						.and(leadHistory.id.roomSequence.eq(guestId.getRoomSequence()))
						.and(leadHistory.id.reservationConfirmationNum.eq(guestId.getReservationConfirmationNum())))
				.fetch();

	}

	@Override
	public List<LeadContactHistory> findAllComments(String organizationId, Integer propertyId,
			Integer customerUniqueId) {

		QLeadContactHistory leadHistory = new QLeadContactHistory("LEAD");
		QLeadStatus leadStatus = new QLeadStatus("leadStatus");
		QPerson allocated = new QPerson("person");
		QPerson initialAllocated = new QPerson("person");
		QPerson initialContacted = new QPerson("person");
		QPerson createPerson = new QPerson("person");
		QActivator activator = new QActivator("activator");

		return getQueryFactory().selectFrom(leadHistory).leftJoin(leadHistory.leadStatus, leadStatus).fetchJoin()
				.leftJoin(leadHistory.allocatedPerson, allocated).fetchJoin()
				.leftJoin(leadHistory.initialAllocatedPerson, initialAllocated).fetchJoin()
				.leftJoin(leadHistory.initialContactedPerson, initialContacted).fetchJoin()
				.leftJoin(leadHistory.activator, activator).fetchJoin().leftJoin(leadHistory.createPerson, createPerson)
				.fetchJoin().orderBy(leadHistory.id.createDtm.desc()).where(leadHistory.id.organizationId
						.eq(organizationId).and(leadHistory.customerUniqueId.eq(customerUniqueId)))
				.fetch();
	}

	@Override
	public List<LeadContact> findByIds(Set<GuestId> ids) {

		return null;
	}

	@Override
	public List<LeadContactHistory> findByOrganizationIdAndPropertyId(String organizationId, Integer propertyId) {

		return (repository).findByIdOrganizationIdAndIdPropertyId(organizationId, propertyId);
	}

	@Override
	public List<LeadContactHistory> getLeadContactReservationData(String organizationId, Integer propertyId,
			String resNum, Integer roomNumber, Integer guestNumber) {

		QLeadContactHistory leadContactHistory = new QLeadContactHistory("LEADCONTACTHISTORY");
		JPAQuery<LeadContactHistory> query = new JPAQuery<LeadContactHistory>(getEntityManager())
				.from(leadContactHistory)
				.where(leadContactHistory.id.reservationConfirmationNum.eq(resNum)
						.and(leadContactHistory.id.roomSequence.eq(roomNumber))
						.and(leadContactHistory.id.guestSequence.eq(guestNumber)));
		return query.fetch();

	}

	@Override
	protected CrudRepository<LeadContactHistory, LeadContactHistoryId> getRepository() {

		return repository;
	}

	@Override
	public Set<LeadContactHistory> saveNonTransactional(Iterable<LeadContactHistory> entities) {

		Set<LeadContactHistory> saved = new HashSet<>();
		for (LeadContactHistory lch : entities) {
			getEntityManager().detach(lch);
			saved.add(save(lch));
		}
		return IterableHelper.convertSet(saved);
	}

	@Override
	public Set<LeadContactHistory> saveRaw(Set<LeadContactHistory> histories) throws SQLException {

		Set<LeadContactHistory> saved = new HashSet<>();

		Connection connection = getEntityManager().unwrap(SessionImpl.class).connection();
		String strQuery = "INSERT INTO LeadContactHistory (" + " LeadContactHistory.OrganizationId," // 1
				+ " LeadContactHistory.PropertyId," // 2
				+ " LeadContactHistory.ReservationConfirmationNum," // 3
				+ " LeadContactHistory.RoomSequence," // 4
				+ " LeadContactHistory.GuestSequence," // 5
				+ " LeadContactHistory.CreateDtm," // 6
				+ " LeadContactHistory.CustomerUniqueId," // 7
				+ " LeadContactHistory.StayId," // 8
				+ " LeadContactHistory.GuestStatus," // 9
				+ " LeadContactHistory.RateCode," // 10
				+ " LeadContactHistory.GuestTypeCode," // 11
				+ " LeadContactHistory.InternalPropertyId," // 12
				+ " LeadContactHistory.TripTicketNumber," // 13
				+ " LeadContactHistory.ShowDate," // 14
				+ " LeadContactHistory.Language," // 15
				+ " LeadContactHistory.ArrivalDate," // 16
				+ " LeadContactHistory.DepartureDate," // 17
				+ " LeadContactHistory.OverrideGuestTypeFlag," // 18
				+ " LeadContactHistory.ContactTypeCode," // 19
				+ " LeadContactHistory.ActivatorId," // 20
				+ " LeadContactHistory.LeadStatusCode," // 21
				+ " LeadContactHistory.OverrideLeadStatusFlag," // 22
				+ " LeadContactHistory.ContactFlag," // 23
				+ " LeadContactHistory.InvitedFlag," // 24
				+ " LeadContactHistory.ShowOnTripTicketFlag," // 25
				+ " LeadContactHistory.PromiseGiftFlag," // 26
				+ " LeadContactHistory.IssueGiftFlag," // 27
				+ " LeadContactHistory.OnsiteMarketingLocationId," // 28
				+ " LeadContactHistory.RemarksText," // 29
				+ " LeadContactHistory.InitialAllocatedPersonId," // 30
				+ " LeadContactHistory.AllocatedPersonId," // 31
				+ " LeadContactHistory.InitialContactedPersonId," // 32
				+ " LeadContactHistory.EnhancedFlag," // 33
				+ " LeadContactHistory.SystemFlag," // 34
				+ " LeadContactHistory.ActiveFlag," // 35
				+ " LeadContactHistory.ODSUpdateDtm," // 36
				+ " LeadContactHistory.ODSPackageId," // 37
				+ " LeadContactHistory.CreatePersonId," // 38
				+ " LeadContactHistory.ConciergeSequenceId," // 39
				+ " LeadContactHistory.Seed," // 40
				+ " LeadContactHistory.OptInFlag," // 41
				+ " LeadContactHistory.OptInLastRefreshDtm," // 42
				+ " LeadContactHistory.BookingDtm," // 43
				+ " LeadContactHistory.RoomName," // 44
				+ " LeadContactHistory.RoomNumber)" // 45
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = null;
		preparedStatement = connection.prepareStatement(strQuery);
		connection.setAutoCommit(false);
		for (LeadContactHistory lc : histories) {
			Timestamp now = convertSqlDate(new Date());
			if (lc.getId() != null) {
				preparedStatement.setString(1, lc.getId().getOrganizationId());
				if (lc.getId().getPropertyId() == null) {
					preparedStatement.setNull(2, Types.INTEGER);
				} else {
					preparedStatement.setInt(2, lc.getId().getPropertyId());
				}
				preparedStatement.setString(3, lc.getId().getReservationConfirmationNum());
				if (lc.getId().getRoomSequence() == null) {
					preparedStatement.setNull(4, Types.INTEGER);
				} else {
					preparedStatement.setInt(4, lc.getId().getRoomSequence());
				}
				if (lc.getId().getGuestSequence() == null) {
					preparedStatement.setNull(5, Types.INTEGER);

				} else {
					preparedStatement.setInt(5, lc.getId().getGuestSequence());
				}
				if (lc.getId().getCreateDtm() == null) {
					// preparedStatement.setNull(6, Types.DATE);
					preparedStatement.setTimestamp(6, convertSqlDate(now));
				} else {
					preparedStatement.setTimestamp(6, convertSqlDate(now));
				}
			} else {
				preparedStatement.setNull(1, Types.VARCHAR);
				preparedStatement.setNull(2, Types.INTEGER);
				preparedStatement.setNull(3, Types.VARCHAR);
				preparedStatement.setNull(4, Types.INTEGER);
				preparedStatement.setNull(5, Types.INTEGER);
				preparedStatement.setNull(6, Types.DATE);
			}
			if (lc.getCustomerUniqueId() == null) {
				preparedStatement.setNull(7, Types.INTEGER);
			} else {
				preparedStatement.setInt(7, lc.getCustomerUniqueId());
			}
			if (lc.getStayId() == null) {
				preparedStatement.setNull(8, Types.INTEGER);
			} else {
				preparedStatement.setInt(8, lc.getStayId());
			}
			preparedStatement.setString(9, lc.getGuestStatus());
			preparedStatement.setString(10, lc.getRateCode());
			preparedStatement.setString(11, lc.getGuestTypeCode());
			preparedStatement.setString(12, lc.getInternalPropertyId());
			preparedStatement.setString(13, lc.getTripTicketNumber());
			if (lc.getShowDate() == null) {
				preparedStatement.setNull(14, Types.DATE);
			} else {
				preparedStatement.setTimestamp(14, convertSqlDate(lc.getShowDate()));
			}
			preparedStatement.setString(15, lc.getLanguage());
			if (lc.getArrivalDate() == null) {
				preparedStatement.setNull(16, Types.DATE);
			} else {
				preparedStatement.setTimestamp(16, convertSqlDate(lc.getArrivalDate()));
			}
			if (lc.getDepartureDate() == null) {
				preparedStatement.setNull(17, Types.DATE);
			} else {
				preparedStatement.setTimestamp(17, convertSqlDate(lc.getDepartureDate()));
			}
			if (lc.getOverrideGuestTypeFlag() == null) {
				preparedStatement.setNull(18, Types.BOOLEAN);
			} else {
				preparedStatement.setBoolean(18, lc.getOverrideGuestTypeFlag());
			}
			preparedStatement.setString(19, lc.getContactTypeCode());
			if (lc.getActivatorId() == null) {
				preparedStatement.setNull(20, Types.INTEGER);
			} else {
				preparedStatement.setInt(20, lc.getActivatorId());
			}
			preparedStatement.setString(21, lc.getLeadStatusCode());
			if (lc.getOverrideLeadStatusFlag()) {
				preparedStatement.setBoolean(22, false);
			} else {
				preparedStatement.setBoolean(22, lc.getOverrideLeadStatusFlag());
			}
			if (lc.getContactFlag() == null) {
				preparedStatement.setNull(23, Types.BOOLEAN);
			} else {
				preparedStatement.setBoolean(23, lc.getContactFlag());
			}
			if (lc.getInvitedFlag() == null) {
				preparedStatement.setNull(24, Types.BOOLEAN);
			} else {
				preparedStatement.setBoolean(24, lc.getInvitedFlag());
			}
			if (lc.getShowOnTripTicketFlag() == null) {
				preparedStatement.setNull(25, Types.BOOLEAN);
			} else {
				preparedStatement.setBoolean(25, lc.getShowOnTripTicketFlag());
			}
			if (lc.getPromiseGiftFlag() == null) {
				preparedStatement.setNull(26, Types.BOOLEAN);
			} else {
				preparedStatement.setBoolean(26, lc.getPromiseGiftFlag());
			}
			if (lc.getIssueGiftFlag() == null) {
				preparedStatement.setNull(27, Types.BOOLEAN);
			} else {
				preparedStatement.setBoolean(27, lc.getIssueGiftFlag());
			}
			if (lc.getOnsiteMarketingLocationId() == null) {
				preparedStatement.setNull(28, Types.INTEGER);
			} else {
				preparedStatement.setInt(28, lc.getOnsiteMarketingLocationId());
			}
			preparedStatement.setString(29, lc.getRemarksText());

			if (lc.getInitialAllocatedPersonId() == null) {
				preparedStatement.setNull(30, Types.INTEGER);
			} else {
				preparedStatement.setInt(30, lc.getInitialAllocatedPersonId());
			}
			if (lc.getAllocatedPersonId() == null) {
				preparedStatement.setNull(31, Types.INTEGER);
			} else {
				preparedStatement.setInt(31, lc.getAllocatedPersonId());
			}
			if (lc.getInitialContactedPersonId() == null) {
				preparedStatement.setNull(32, Types.INTEGER);
			} else {
				preparedStatement.setInt(32, lc.getInitialContactedPersonId());
			}
			if (lc.getEnhancedFlag() == null) {
				preparedStatement.setNull(33, Types.BOOLEAN);
			} else {
				preparedStatement.setBoolean(33, lc.getEnhancedFlag());
			}
			preparedStatement.setBoolean(34, false);
			if (lc.getActiveFlag() == null) {
				preparedStatement.setNull(35, Types.BOOLEAN);
			} else {
				preparedStatement.setBoolean(35, lc.getActiveFlag());
			}
			if (lc.getOdsUpdateDtm() == null) {
				preparedStatement.setNull(36, Types.DATE);
			} else {
				preparedStatement.setTimestamp(36, convertSqlDate(lc.getOdsUpdateDtm()));
			}
			if (lc.getOdsPackageId() == null) {
				preparedStatement.setNull(37, Types.LONGVARCHAR);
			} else {
				preparedStatement.setLong(37, lc.getOdsPackageId());
			}
			if (lc.getCreatePersonId() == null) {
				preparedStatement.setInt(38, 0); // TODO This should happen in
													// the objects
													// constructor.
			} else {
				preparedStatement.setInt(38, lc.getCreatePersonId());
			}
			if (lc.getConciergeSequenceId() == null) {
				preparedStatement.setNull(39, Types.LONGVARCHAR);
			} else {
				preparedStatement.setLong(39, lc.getConciergeSequenceId());
			}
			if (lc.getSeed() == null) {
				preparedStatement.setNull(40, Types.BOOLEAN);
			} else {
				preparedStatement.setBoolean(40, lc.getSeed());
			}
			if (lc.getOptInFlag() == null) {
				preparedStatement.setNull(41, Types.BOOLEAN);
			} else {
				preparedStatement.setBoolean(41, lc.getOptInFlag());
			}
			if (lc.getOptInLastRefreshDtm() == null) {
				preparedStatement.setNull(42, Types.DATE);
			} else {
				preparedStatement.setTimestamp(42, convertSqlDate(lc.getOptInLastRefreshDtm()));
			}
			if (lc.getBookingDtm() == null) {
				preparedStatement.setNull(43, Types.DATE);
			} else {
				preparedStatement.setTimestamp(43, convertSqlDate(lc.getBookingDtm()));
			}
			preparedStatement.setString(44, lc.getRoomName());
			preparedStatement.setString(45, lc.getRoomNumber());
			preparedStatement.addBatch();
			saved.add(lc);
		}
		StopWatch watch = new StopWatch();
		watch.start();
		preparedStatement.executeBatch();
		watch.stop();
		LOGGER.log(Level.INFO, "ASTIME Lead History Batch - Elapsed time in seconds: " + watch.getTotalTimeSeconds());
		return saved;
	}

}
