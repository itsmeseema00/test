package com.vistana.onsiteconcierge.core.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQuery;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dao.ReservationRepository;
import com.vistana.onsiteconcierge.core.model.Guest;
import com.vistana.onsiteconcierge.core.model.GuestId;
import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.model.QGuest;
import com.vistana.onsiteconcierge.core.model.QReservation;
import com.vistana.onsiteconcierge.core.model.QRoom;
import com.vistana.onsiteconcierge.core.model.QStay;
import com.vistana.onsiteconcierge.core.model.Rate;
import com.vistana.onsiteconcierge.core.model.RateId;
import com.vistana.onsiteconcierge.core.model.Reservation;
import com.vistana.onsiteconcierge.core.model.ReservationId;
import com.vistana.onsiteconcierge.core.model.Room;
import com.vistana.onsiteconcierge.core.model.Stay;
import com.vistana.onsiteconcierge.core.service.GuestService;
import com.vistana.onsiteconcierge.core.service.LeadContactService;
import com.vistana.onsiteconcierge.core.service.LookupService;
import com.vistana.onsiteconcierge.core.service.OwnerContractService;
import com.vistana.onsiteconcierge.core.service.OwnerInventoryService;
import com.vistana.onsiteconcierge.core.service.OwnerService;
import com.vistana.onsiteconcierge.core.service.PackageService;
import com.vistana.onsiteconcierge.core.service.RateService;
import com.vistana.onsiteconcierge.core.service.ReservationService;
import com.vistana.onsiteconcierge.core.service.RoomService;
import com.vistana.onsiteconcierge.core.service.StayService;
import com.vistana.onsiteconcierge.core.service.TourEmailService;
import com.vistana.onsiteconcierge.core.service.TourGiftService;
import com.vistana.onsiteconcierge.core.service.TourService;
import com.vistana.onsiteconcierge.core.service.TourXrefService;

@Service
public class ReservationServiceImpl extends SaveDeleteServiceImpl<Reservation, ReservationId>
		implements ReservationService {

	@Autowired
	public GuestService guestService;

	@Autowired
	public LeadContactService leadContactService;

	@Autowired
	protected OwnerContractService ownerContractService;

	@Autowired
	protected OwnerInventoryService ownerInventoryService;

	@Autowired
	protected OwnerService ownerService;

	@Autowired
	protected PackageService packageService;

	@Autowired
	public RateService rateService;

	@Autowired
	private ReservationRepository repository;

	@Autowired
	protected ReservationService reservationService;

	@Autowired
	public RoomService roomService;

	@Autowired
	public StayService stayService;

	@Autowired
	protected TourEmailService tourEmailService;

	@Autowired
	protected TourGiftService tourGiftService;

	@Autowired
	public TourService tourService;

	@Autowired
	protected TourXrefService tourXrefService;

	@Override
	public List<Reservation> findFullyLoaded(List<ReservationId> ids) {

		QReservation reservation = new QReservation("RESERVATION");
		QRoom room = new QRoom("ROOM");
		QGuest guest = new QGuest("GUEST");
		QStay stay = new QStay("STAY");

		return getQueryFactory().selectFrom(reservation).leftJoin(reservation.rooms, room).fetchJoin()
				.leftJoin(room.guests, guest).fetchJoin().leftJoin(guest.stay, stay).fetchJoin()
				.where(reservation.id.in(ids)).fetch();
	}

	@Override
	public Reservation findFullyLoaded(ReservationId id) {

		QReservation reservation = new QReservation("RESERVATION");
		QRoom room = new QRoom("ROOM");
		QGuest guest = new QGuest("GUEST");
		QStay stay = new QStay("STAY");

		return getQueryFactory().selectFrom(reservation).leftJoin(reservation.rooms, room).fetchJoin()
				.leftJoin(room.guests, guest).fetchJoin().leftJoin(guest.stay, stay).fetchJoin()
				.where(reservation.id.eq(id)).fetchOne();
	}

	public int getIndexOfMin(List<Guest> guest) {

		int min = Integer.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < guest.size(); i++) {
			Integer ii = guest.get(i).getId().getGuestSequence();
			if (Integer.compare(ii.intValue(), min) < 0) {
				min = ii.intValue();
				index = i;
			}
		}
		return index;
	}

	@Override
	protected CrudRepository<Reservation, ReservationId> getRepository() {

		return repository;
	}

	@Override
	public List<Reservation> getReservationData(String organizationId, int propertyId, String resConfirmationNum) {

		QReservation res = new QReservation("RESERVATION");
		JPAQuery<Reservation> query = new JPAQuery<Reservation>(getEntityManager()).from(res)
				.where(res.id.reservationConfirmationNum.eq(resConfirmationNum));
		return query.fetch();

	}

	@Override
	public Reservation nonTransSave(Reservation res) {
		return this.repository.save(res);
	}

	@Deprecated
	@Override
	@Transactional
	public List<Reservation> updateFully(List<Reservation> reservationsIncoming) {

		/*
		 * Retrieve latest set of reservations from Input JSON. If multiple
		 * records for same reservations number exist in list: non-seed trumps
		 * seed, highest sequence trumps lower sequence
		 */
		List<Reservation> reservationsDeduplicated = new ArrayList<>();
		reservationsIncoming.forEach(r -> {
			if (reservationsDeduplicated.contains(r)) {
				Reservation found = reservationsDeduplicated.get(reservationsDeduplicated.indexOf(r));
				if (found.isOlder(r)) {
					reservationsDeduplicated.remove(found);
				} else {
					return;
				}
			}
			reservationsDeduplicated.add(r);
		});

		List<RateId> rateIds = reservationsDeduplicated.stream().map(one -> new RateId(one))
				.collect(Collectors.toList());
		List<Rate> rates = rateService.findAll(rateIds);

		/*
		 * Compare the reservations from input feed with the reservations in
		 * database to identify the latest sequence number / seeded and persist
		 * if the reservations from input feed is the latest.
		 */
		List<Reservation> persisted = new ArrayList<>();
		reservationsDeduplicated.forEach(incoming -> {

			/* Retrieve existing reservation. */
			Reservation existing = findFullyLoaded(incoming.getId());
			if (existing != null) {
				if (existing.isOlder(incoming)) {
					Guest guestDedupPrimary = incoming.getPrimaryGuest();
					Guest guestDbPrimary = existing.getPrimaryGuest();
					if (guestDedupPrimary != null && guestDbPrimary != null) {
						guestDedupPrimary.setStay(guestDbPrimary.getStay());
					}
				} else {
					return;
				}
			}

			Rate toFind = new Rate(incoming);
			String guestTypeCode = CoreConstants.GUEST_TYPE_UNKNOWN;
			if (rates.contains(toFind)) {
				Rate rate = rates.get(rates.indexOf(toFind));
				guestTypeCode = rate.getGuestTypeCode();
			}

			Room roomPrimary = incoming.getPrimaryRoom();
			Guest guestPrimary = incoming.getPrimaryGuest();
			LeadContact leadContact = null;
			if (roomPrimary != null && guestPrimary != null && (guestPrimary.getStay() == null
					|| (guestPrimary.getStay() != null && !guestPrimary.getStay().getEnhancedFlag()))) {
				// stay does not exist or not enhanced by ODS
				Stay stay = new Stay(incoming, roomPrimary);
				Stay saved = stayService.save(stay);

				guestPrimary.setStayId(saved.getId());

				leadContact = new LeadContact(saved, incoming, roomPrimary, guestPrimary, guestTypeCode);
				leadContact.setStayId(saved.getId());

				// to send stay data to ODS for profiling
				guestPrimary.setStay(saved);
			}
			getRepository().save(incoming);
			roomService.save(incoming.getRooms());
			guestService.save(incoming.getGuests());
			leadContactService.save(leadContact);

			persisted.add(incoming);
		});

		return persisted;
	}

	@Override
	@Transactional
	public Reservation updateFully(Reservation incoming) throws SQLException {

		String guestTypeCode = CoreConstants.GUEST_TYPE_UNKNOWN;

		/*
		 * Compare the reservations from input feed with the reservations in
		 * database to identify the latest sequence number / seeded and persist
		 * if the reservations from input feed is the latest.
		 */
		Reservation persisted = null;
		List<Room> roomsToSave = new ArrayList<>();
		List<Guest> guestsToSave = new ArrayList<>();
		List<LeadContact> leadsToSave = new ArrayList<>();

		/* Retrieve existing reservation. */
		Reservation existing = findFullyLoaded(incoming.getId());
		if (existing == null) {
			Room roomPrimary = incoming.getPrimaryRoom();
			Stay stay = new Stay(incoming, roomPrimary);
			stay.setConciergeSequenceId(incoming.getConciergeSequenceId());
			stay.setSeed(incoming.getSeed());
			stay = stayService.nonTransSave(stay);

			for (Room room : incoming.getRooms()) {
				room.setConciergeSequenceId(incoming.getConciergeSequenceId());
				room.setSeed(incoming.getSeed());
				roomsToSave.add(room);

				int guestIndexOfMin = getIndexOfMin(room.getGuests().stream().collect(Collectors.toList()));

				int i = 0;
				for (Guest guest : room.getGuests()) {
					guest.setConciergeSequenceId(incoming.getConciergeSequenceId());
					guest.setSeed(incoming.getSeed());
					guest.setStayId(stay.getId());
					guest.setStay(stay);
					guestsToSave.add(guest);

					// all guests not first in room are "Room Sharer"
					LeadContact lead = new LeadContact(stay, incoming, room, guest, null);
					lead.setConciergeSequenceId(incoming.getConciergeSequenceId());
					lead.setSeed(incoming.getSeed());

					lead.setRoomNumber(room.getRoomNumber());
					lead.setRoomName(room.getRoomName());

					if (incoming.isRoomSharer(room, guest) && i != guestIndexOfMin) {
						lead.setGuestTypeCode(LookupService.GUEST_TYPE_ROOM_SHARER);
						lead.setOverrideGuestTypeFlag(true);

					} else {
						if (lead.getGuestTypeCode() == null) {
							lead.setGuestTypeCode(guestTypeCode);
						}
						lead.setOverrideGuestTypeFlag(lead.getOverrideGuestTypeFlag());
					}
					lead.setOwners(null);
					lead.setPackages(null);
					lead.setLinkedTours(null);
					leadsToSave.add(lead);

					i++;
				}
			}

			persisted = this.nonTransSave(incoming);
			roomService.nonTransSave(roomsToSave);
			guestService.nonTransSave(guestsToSave);
			leadContactService.nonTransSave(leadsToSave);
		} else if (existing != null && existing.isOlder(incoming)) {
			// TODO: need to know what to do if a room / guest is removed from a
			// reservation
			Room roomPrimary = incoming.getPrimaryRoom();
			Guest guestPrimary = existing.getPrimaryGuest();

			Stay stay = stayService.find(guestPrimary.getStayId());
			stay.setConciergeSequenceId(incoming.getConciergeSequenceId());
			stay.setSeed(incoming.getSeed());
			stay.setStay(incoming, roomPrimary);
			stay = stayService.save(stay);

			List<GuestId> guestIds = existing.getGuests().stream().map(one -> one.getId()).collect(Collectors.toList());
			List<LeadContact> leads = leadContactService.findAll(guestIds);

			for (Room room : incoming.getRooms()) {
				room.setConciergeSequenceId(incoming.getConciergeSequenceId());
				room.setSeed(incoming.getSeed());
				roomsToSave.add(room);

				int guestIndexOfMin = getIndexOfMin(room.getGuests().stream().collect(Collectors.toList()));
				int i = 0;
				for (Guest guest : room.getGuests()) {
					guest.setConciergeSequenceId(incoming.getConciergeSequenceId());
					guest.setSeed(incoming.getSeed());
					guest.setStayId(stay.getId());
					guest.setStay(stay);

					LeadContact lead = null;
					if (leads.contains(new LeadContact(stay, incoming, room, guest, null))) {
						LeadContact ogLead = leads
								.get(leads.indexOf(new LeadContact(stay, incoming, room, guest, null)));
						guest.setCustomerUniqueId(ogLead.getCustomerUniqueId());
						lead = leads.remove(leads.indexOf(new LeadContact(stay, incoming, room, guest, null)));
						lead.setLeadContact(stay, incoming, room, guest, ogLead.getGuestTypeCode());
						lead.setLeadContact(ogLead);
						lead.setGuestTypeCode(ogLead.getGuestTypeCode());
					} else {
						lead = new LeadContact(stay, incoming, room, guest, null);
					}
					lead.setConciergeSequenceId(incoming.getConciergeSequenceId());
					lead.setSeed(incoming.getSeed());

					lead.setRoomNumber(room.getRoomNumber());
					lead.setRoomName(room.getRoomName());

					if (incoming.isRoomSharer(room, guest) && i != guestIndexOfMin) {
						lead.setGuestTypeCode(LookupService.GUEST_TYPE_ROOM_SHARER);
						lead.setOverrideGuestTypeFlag(true);

					} else {
						if (lead.getGuestTypeCode() == null) {
							lead.setGuestTypeCode(guestTypeCode);
						}
						lead.setOverrideGuestTypeFlag(lead.getOverrideGuestTypeFlag());
					}
					guestsToSave.add(guest);
					leads.add(lead);
					i++;
				}
			}

			leads.forEach(lead -> {
				lead.setOwners(null);
				lead.setPackages(null);
				lead.setLinkedTours(null);
			});
			leadsToSave.addAll(leads);

			persisted = this.nonTransSave(incoming);
			roomService.nonTransSave(roomsToSave);
			guestService.nonTransSave(guestsToSave);
			leadContactService.nonTransSave(leadsToSave);
		}

		return persisted;
	}

	@Override
	public void updateReservationOdsFlag(List<Reservation> reservations) {

		getRepository().save(reservations);
	}

}
