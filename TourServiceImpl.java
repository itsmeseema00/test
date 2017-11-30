package com.vistana.onsiteconcierge.core.service.impl;

import static com.vistana.onsiteconcierge.core.CoreConstants.LEAD_STATUS_BOUGHT;
import static com.vistana.onsiteconcierge.core.CoreConstants.LEAD_STATUS_NO_SHOW;
import static com.vistana.onsiteconcierge.core.CoreConstants.LEAD_STATUS_TOURED;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dao.TourCommentRepository;
import com.vistana.onsiteconcierge.core.dao.TourEmailRepository;
import com.vistana.onsiteconcierge.core.dao.TourGiftRepository;
import com.vistana.onsiteconcierge.core.dao.TourRepository;
import com.vistana.onsiteconcierge.core.dao.TourXrefRepository;
import com.vistana.onsiteconcierge.core.dto.TourPackageDto;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.model.LeadContactHistory;
import com.vistana.onsiteconcierge.core.model.QTour;
import com.vistana.onsiteconcierge.core.model.Tour;
import com.vistana.onsiteconcierge.core.model.TourComment;
import com.vistana.onsiteconcierge.core.model.TourEmail;
import com.vistana.onsiteconcierge.core.model.TourGift;
import com.vistana.onsiteconcierge.core.model.TourId;
import com.vistana.onsiteconcierge.core.model.TourXref;
import com.vistana.onsiteconcierge.core.service.LeadContactHistoryService;
import com.vistana.onsiteconcierge.core.service.LeadContactService;
import com.vistana.onsiteconcierge.core.service.TourService;

/**
 *
 */
@Service
public class TourServiceImpl extends SaveDeleteServiceImpl<Tour, TourId> implements TourService {

	@Autowired
	private LeadContactService leadContactService;

	@Autowired
	private TourRepository repository;

	@Autowired
	private TourXrefRepository tourXrefRepository;

	@Autowired
	private TourCommentRepository tourCommentRepository;

	@Autowired
	private TourEmailRepository tourEmailRepository;

	@Autowired
	private TourGiftRepository tourGiftRepository;

	@Autowired
	private LeadContactHistoryService leadContactHistoryService;

	@Override
	public List<Tour> findByOrganizationAndPropertyAndTourNumbers(String organization, String property,
			List<String> tourNumbers) {

		QTour tour = new QTour("TOUR");
		return getQueryFactory()
				.selectFrom(tour).where(tour.id.organizationId.eq(organization)
						.and(tour.id.internalPropertyId.eq(property)).and(tour.id.tripTicketNumber.in(tourNumbers)))
				.fetch();
	}

	@Override
	public Tour findByOrganizationAndPropertyAndTripTicket(String organization, String property, String tripTicket) {

		QTour tour = new QTour("TOUR");
		return getQueryFactory().selectFrom(tour).where(tour.id.organizationId.eq(organization)
				.and(tour.propertyId.eq(Integer.valueOf(property))).and(tour.id.tripTicketNumber.eq(tripTicket)))
				.fetchOne();
	}

	@Override
	public List<TourPackageDto> findByOrganizationIdAndTripTicketNumber(String organization, String tourNumber) {

		return repository.findByIdOrganizationIdAndIdTripTicketNumber(organization, tourNumber);
	}

	@Override
	protected CrudRepository<Tour, TourId> getRepository() {

		return repository;
	}

	@Override
	public void nonTransSave(List<Tour> tours) {

		// TODO Auto-generated method stub
		this.repository.save(tours);
	}

	@Override
	@Transactional
	public void save(List<Tour> tours, String organization, Integer property, List<Tour> listToured,
			List<Tour> listNoShow, List<Tour> listBought) {

		save(tours);

		if (listToured.size() > 0) {
			leadContactService.updateLeadStatus(organization, property, listToured, LEAD_STATUS_TOURED);
		}
		if (listNoShow.size() > 0) {
			leadContactService.updateLeadStatus(organization, property, listNoShow, LEAD_STATUS_NO_SHOW);
		}
		if (listBought.size() > 0) {
			leadContactService.updateLeadStatus(organization, property, listBought, LEAD_STATUS_BOUGHT);
		}
	}

	@Override
	@Transactional
	public Tour linkGuest(TourId tourId, LeadContact leadContact) {

		if (leadContact.getId() == null) {
			return null;
		}
		Tour foundTour = find(tourId);
		Tour savedTour = null;
		leadContact = leadContactService.find(leadContact.getId());
		LeadContactHistory leadContactHistory = new LeadContactHistory(leadContact);
		Integer numTours = leadContact.getLinkedTours() != null ? leadContact.getTours().size() : 0;
		if (foundTour != null) {
			foundTour.setOverrideFlag(true);
			foundTour.setCustomerUniqueId(leadContact.getCustomerUniqueId());
			foundTour.setRoomSequence(leadContact.getId().getRoomSequence());
			foundTour.setGuestSequence(leadContact.getId().getGuestSequence());
			foundTour.setPropertyId(leadContact.getId().getPropertyId());
			foundTour.setReservationConfirmationNum(leadContact.getId().getReservationConfirmationNum());
			savedTour = save(foundTour);
			leadContactHistory.setSystemFlag(true);
			leadContactHistory.setRemarksText("Linked Tour: " + tourId.getTripTicketNumber());
			if (foundTour.getShowDate()!=null & leadContact.getStay()!=null && leadContact.getStay().getStayArrivalDate()!=null && leadContact.getStay().getStayDepartureDate()!=null) {
				if(foundTour.getShowDate().compareTo(leadContact.getStay().getStayArrivalDate())>=0&&foundTour.getShowDate().compareTo(leadContact.getStay().getStayDepartureDate())<=0){
					leadContactService.updateLeadStatus(savedTour, null,
							CoreConstants.LeadStatusConstants.LEAD_STATUS_RMK_LINKED, leadContact.getId());
				}

			}
			if (savedTour != null) {
				leadContactHistoryService.save(leadContactHistory);
			}
			// TODO Link tour comment here?
		} else {
			throw new InvalidClientRequest("Could not link tour to guest");
		}

		return savedTour;
	}

	@Override
	@Transactional
	public void saveCascade(Tour tour, String organization, String property) {

		save(tour);
		Set<TourXref> tourXrefs = tour.getTourXrefs();
		if (!tourXrefs.isEmpty()) {
			tourXrefRepository.save(tourXrefs);
		}
		Set<TourComment> tourComments = tour.getTourComments();
		if (!tourComments.isEmpty()) {
			tourCommentRepository.save(tourComments);
		}
		Set<TourEmail> tourEmails = tour.getTourEmails();
		if (!tourEmails.isEmpty()) {
			tourEmailRepository.save(tourEmails);
		}
		Set<TourGift> tourGifts = tour.getTourGifts();
		if (!tourGifts.isEmpty()) {
			tourGiftRepository.save(tourGifts);
		}

	}

}
