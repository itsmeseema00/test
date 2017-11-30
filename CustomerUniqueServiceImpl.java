package com.vistana.onsiteconcierge.core.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dao.CustomerUniqueRepository;
import com.vistana.onsiteconcierge.core.model.CustomerUnique;
import com.vistana.onsiteconcierge.core.model.CustomerUniqueId;
import com.vistana.onsiteconcierge.core.model.Guest;
import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.model.Owner;
import com.vistana.onsiteconcierge.core.model.OwnerContract;
import com.vistana.onsiteconcierge.core.model.OwnerInventory;
import com.vistana.onsiteconcierge.core.model.Package;
import com.vistana.onsiteconcierge.core.model.PackageGift;
import com.vistana.onsiteconcierge.core.model.Tour;
import com.vistana.onsiteconcierge.core.model.TourComment;
import com.vistana.onsiteconcierge.core.model.TourEmail;
import com.vistana.onsiteconcierge.core.model.TourGift;
import com.vistana.onsiteconcierge.core.model.TourXref;
import com.vistana.onsiteconcierge.core.service.CustomerUniqueService;
import com.vistana.onsiteconcierge.core.service.GuestService;
import com.vistana.onsiteconcierge.core.service.LeadContactService;
import com.vistana.onsiteconcierge.core.service.OwnerContractService;
import com.vistana.onsiteconcierge.core.service.OwnerInventoryService;
import com.vistana.onsiteconcierge.core.service.OwnerService;
import com.vistana.onsiteconcierge.core.service.PackageGiftService;
import com.vistana.onsiteconcierge.core.service.PackageService;
import com.vistana.onsiteconcierge.core.service.TourCommentService;
import com.vistana.onsiteconcierge.core.service.TourEmailService;
import com.vistana.onsiteconcierge.core.service.TourGiftService;
import com.vistana.onsiteconcierge.core.service.TourService;
import com.vistana.onsiteconcierge.core.service.TourXrefService;

@Service
public class CustomerUniqueServiceImpl extends SaveDeleteServiceImpl<CustomerUnique, CustomerUniqueId>

		implements CustomerUniqueService {

	private static final Boolean FALSE = false;

	@Autowired
	protected GuestService guestService;

	@Autowired
	protected LeadContactService leadContactService;

	@Autowired
	protected OwnerContractService ownerContractService;

	@Autowired
	protected OwnerInventoryService ownerInventoryService;

	@Autowired
	protected OwnerService ownerService;

	@Autowired
	protected PackageGiftService packageGiftService;

	@Autowired
	protected PackageService packageService;

	@Autowired
	protected CustomerUniqueRepository repository;

	@Autowired
	protected TourCommentService tourCommentService;

	@Autowired
	protected TourEmailService tourEmailService;

	@Autowired
	protected TourGiftService tourGiftService;

	@Autowired
	protected TourService tourService;

	@Autowired
	protected TourXrefService tourXrefService;

	private void clearPast(List<Owner> owners, List<Package> packages) throws SQLException {
		Iterator<Owner> own = owners.iterator();
		while (own.hasNext()) {
			ownerService.deleteFullyNonTrans(own.next());
		}

		Iterator<Package> packs = packages.iterator();
		while (packs.hasNext()) {
			packageService.deleteFullyNonTrans(packs.next());
		}

	}

	@Override
	protected CrudRepository<CustomerUnique, CustomerUniqueId> getRepository() {

		return repository;
	}

	@Override
	@Transactional
	public CustomerUnique saveFully(CustomerUnique customer, List<Owner> owners, List<Package> packages,
			List<Guest> guests, List<LeadContact> leads) throws SQLException {

		this.clearPast(owners, packages);
		this.saveOwnership(customer, owners);
		this.savePackages(customer, packages);
		this.saveTours(customer);
		this.saveGuests(customer, guests);
		this.saveLeads(customer, leads);

		return customer;
	}

	private void saveGuests(CustomerUnique customer, List<Guest> guests) {
		guests.forEach(guest -> {
			guest.setCustomerUniqueId(customer.getId().getCustomerUniqueId());
			guest.setOdsPackageId(customer.getOdsPackageId());
			guest.setOdsUpdateDtm(customer.getOdsUpdateDtm());
		});
		guestService.nonTransSave(guests);
	}

	private void saveLeads(CustomerUnique customer, List<LeadContact> leads) throws SQLException {
		leads.forEach(lead -> {
			lead.setCustomerUniqueId(customer.getId().getCustomerUniqueId());
			lead.setUpdatePersonId(CoreConstants.SYSTEM_GENERATED);
			lead.setRemarksText(CoreConstants.CUSTOMER_PROFILER_REMARKS_TEXT, FALSE);
			lead.setOdsPackageId(customer.getOdsPackageId());
			lead.setOdsUpdateDtm(customer.getOdsUpdateDtm());
			lead.setOwners(null);
			lead.setPackages(null);
			lead.setLinkedTours(null);
		});
		leadContactService.nonTransSave(leads);
	}

	private void saveOwnership(CustomerUnique customer, List<Owner> owners) throws SQLException {
		if (customer.getOwners() != null) {
			List<OwnerContract> contracts = new ArrayList<>();
			List<OwnerInventory> inventories = new ArrayList<>();
			customer.getOwners().forEach(owner -> {
				if (owner.getOwnerContracts() != null) {
					contracts.addAll(owner.getOwnerContracts());

					owner.getOwnerContracts().forEach(contract -> {
						if (contract.getOwnerInventories() != null) {
							inventories.addAll(contract.getOwnerInventories());
						}
					});
				}
			});
			ownerService.nonTransSave(customer.getOwners());
			ownerContractService.nonTransSave(contracts);
			ownerInventoryService.nonTransSave(inventories);
		}
	}

	private void savePackages(CustomerUnique customer, List<Package> packages) throws SQLException {
		if (customer.getPackages() != null) {
			List<PackageGift> gifts = new ArrayList<>();
			customer.getPackages().forEach(package_ -> {
				if (package_.getPackageGifts() != null) {
					gifts.addAll(package_.getPackageGifts());
				}
			});
			packageService.nonTransSave(customer.getPackages());
			packageGiftService.nonTransSave(gifts);
		}

	}

	private void saveTours(CustomerUnique customer) {
		if (customer.getTours() != null) {
			List<Tour> tours = new ArrayList<>();
			List<TourEmail> tourEmails = new ArrayList<>();
			List<TourGift> tourGifts = new ArrayList<>();
			List<TourXref> tourXrefs = new ArrayList<>();
			List<TourComment> tourComments = new ArrayList<>();
			customer.getTours().forEach(tour -> {
				if (tour.isOverrideFlag()) {
					return;
				}
				tour.setUpdatePersonId(CoreConstants.SYSTEM_GENERATED);
				tours.add(tour);
				if (tour.getTourEmails() != null) {
					tourEmails.addAll(tour.getTourEmails());
				}
				if (tour.getTourGifts() != null) {
					tourGifts.addAll(tour.getTourGifts());
				}
				if (tour.getTourXrefs() != null) {
					tourXrefs.addAll(tour.getTourXrefs());
				}
				if (tour.getTourComments() != null) {
					tourComments.addAll(tour.getTourComments());
				}
			});
			tourService.nonTransSave(tours);
			// tourEmailService.save(tourEmails);
			// tourGiftService.save(tourGifts);
			// tourXrefService.save(tourXrefs);
			// tourCommentService.save(tourComments);
		}
	}

}
