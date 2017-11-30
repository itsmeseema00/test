package com.vistana.onsiteconcierge.core.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.hibernate.internal.SessionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vistana.onsiteconcierge.core.dao.PackageRepository;
import com.vistana.onsiteconcierge.core.model.CustomerUniqueId;
import com.vistana.onsiteconcierge.core.model.GuestId;
import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.model.Package;
import com.vistana.onsiteconcierge.core.model.PackageId;
import com.vistana.onsiteconcierge.core.model.QPackage;
import com.vistana.onsiteconcierge.core.model.QPackageGift;
import com.vistana.onsiteconcierge.core.service.LeadContactService;
import com.vistana.onsiteconcierge.core.service.LookupService;
import com.vistana.onsiteconcierge.core.service.PackageGiftService;
import com.vistana.onsiteconcierge.core.service.PackageService;

@Service
public class PackageServiceImpl extends SaveDeleteServiceImpl<Package, PackageId> implements PackageService {

	@Autowired
	protected LookupService lookupService;

	@Autowired
	protected PackageGiftService packageGiftService;

	@Autowired
	private PackageRepository repository;

	@Autowired
	private LeadContactService leadContactService;

	@Autowired
	private PackageService packageService;

	private java.sql.Date convertSqlDate(Date date) {

		if (date == null) {
			return null;
		} else {
			return new java.sql.Date(date.getTime());
		}
	}

	@Override
	@Transactional
	public void deleteFully(Package package_) {

		if (package_.getPackageGifts() != null) {
			packageGiftService.delete(package_.getPackageGifts());
		}
		getRepository().delete(package_);
	}

	@Override
	public void deleteFullyNonTrans(Package package_) throws SQLException {

		if (package_.getPackageGifts() != null) {
			packageGiftService.nonTransDelete(package_.getPackageGifts());
		}
		this.deleteNonTrans(package_);
	}

	@Override
	public void deleteNonTrans(Package package_) throws SQLException {

		Connection connection = getEntityManager().unwrap(SessionImpl.class).connection();
		try {
			String strQuery = "DELETE from Package WHERE " + " OrganizationId =? " // 0
					+ " AND PackageId =? AND "// 1
					+ "SourceSystemName =?";
			this.getEntityManager().detach(package_);
			PreparedStatement preparedStatement = null;
			preparedStatement = connection.prepareStatement(strQuery);
			connection.setAutoCommit(false);
			PackageId id = package_.getId();
			preparedStatement.setString(1, id.getOrganizationId());
			preparedStatement.setString(2, id.getPackageId());
			preparedStatement.setString(3, id.getSourceSystemName());

			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw e;
		}

	}

	@Override
	public List<Package> findByIdOrganizationIdAndCustomerUniqueId(String organization, Integer customerUniqueId) {

		return repository.findByIdOrganizationIdAndCustomerUniqueId(organization, customerUniqueId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Package> findFullyLoaded(CustomerUniqueId customerId) {

		Query query = getEntityManager()
				.createQuery("SELECT pac FROM Package pac LEFT JOIN FETCH pac.packageGifts gifts"
						+ " WHERE pac.id.organizationId = :org AND pac.customerUniqueId = :cust ");
		query.setParameter("org", customerId.getOrganizationId());
		query.setParameter("cust", customerId.getCustomerUniqueId());
		return query.getResultList();
	}

	@Override
	public List<Package> getPackageAndGifts(String organizationId, Integer custUniqueId) {

		QPackage pack = new QPackage("PACKAGE");
		QPackageGift packageGift = new QPackageGift("packageGift");

		return getQueryFactory().selectFrom(pack).leftJoin(pack.packageGifts, packageGift).fetchJoin()
				.where(pack.id.organizationId.eq(organizationId).and(pack.customerUniqueId.eq(custUniqueId))).distinct()
				.fetch();
	}

	@Override
	protected CrudRepository<Package, PackageId> getRepository() {

		return repository;
	}

	@Override
	@Transactional
	public void linkLead(String organization, Integer propertyId, String resNumber, Integer roomSeq, Integer guestSeq,
			Package pack) {

		GuestId id = new GuestId(organization, propertyId, resNumber, roomSeq, guestSeq);
		LeadContact lead = leadContactService.find(id);
		lead.setPackageId(pack.getId().getPackageId());
		lead.setPackageSourceSystemName(pack.getId().getSourceSystemName());

		leadContactService.save(lead);
		packageService.save(pack);
		packageGiftService.save(pack.getPackageGifts());

	}

	@Override
	public void nonTransSave(Set<Package> packages) throws SQLException {

		Iterator<Package> pack = packages.iterator();
		while (pack.hasNext()) {
			this.saveNative(pack.next());
		}
	}

	private void saveNative(Package package_) throws SQLException {

		Connection connection = getEntityManager().unwrap(SessionImpl.class).connection();
		try {
			String strQuery = "INSERT INTO Package (" + "Package.OrganizationId," + "Package.PackageId,"
					+ "Package.SourceSystemName," + "Package.CustomerUniqueId," + "Package.PackageType,"
					+ "Package.InternalPropertyId," + "Package.PackageNumber," + "Package.TourInternalPropertyId,"
					+ "Package.TripTicketNumber," + "Package.TourDate," + "Package.TourStatus,"
					+ "Package.TourLocation," + "Package.PreferredInvitationNumber," + "Package.SecondaryFirstName,"
					+ "Package.SecondaryLastName," + "Package.PackageGuestType," + "Package.PackageCategoryType,"
					+ "Package.PackageNights," + "Package.PackageStatus," + "Package.PackageStage,"
					+ "Package.Campaign," + "Package.PurchaseDate," + "Package.ExpirationDate," + "Package.ArrivalDate,"
					+ "Package.DepartureDate," + "Package.CancelDate," + "Package.HotelNumber,"
					+ "Package.HotelConfirmationNumber," + "Package.MkgLocationCode," + "Package.ActiveFlag,"
					+ "Package.ODSUpdateDtm," + "Package.ODSPackageId," + "Package.CreateDtm,"
					+ "Package.UpdateDtm ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement preparedStatement = null;
			preparedStatement = connection.prepareStatement(strQuery);
			convertSqlDate(new Date());
			PackageId id = package_.getId();
			preparedStatement.setString(1, id.getOrganizationId());
			preparedStatement.setString(2, id.getPackageId());
			preparedStatement.setString(3, id.getSourceSystemName());
			if (package_.getCustomerUniqueId() != null) {
				preparedStatement.setInt(4, package_.getCustomerUniqueId());
			} else {
				preparedStatement.setNull(4, Types.INTEGER);
			}
			if (package_.getPackageType() != null) {
				preparedStatement.setString(5, package_.getPackageType());
			} else {
				preparedStatement.setNull(5, Types.LONGNVARCHAR);
			}
			if (package_.getInternalPropertyId() != null) {
				preparedStatement.setString(6, package_.getInternalPropertyId());
			} else {
				preparedStatement.setNull(6, Types.LONGNVARCHAR);
			}

			if (package_.getPackageNumber() != null) {
				preparedStatement.setString(7, package_.getPackageNumber());
			} else {
				preparedStatement.setNull(7, Types.LONGNVARCHAR);
			}

			if (package_.getTourInternalPropertyId() != null) {
				preparedStatement.setString(8, package_.getTourInternalPropertyId());
			} else {
				preparedStatement.setNull(8, Types.LONGNVARCHAR);
			}

			if (package_.getTripTicketNumber() != null) {
				preparedStatement.setString(9, package_.getTripTicketNumber());
			} else {
				preparedStatement.setNull(9, Types.LONGNVARCHAR);
			}

			if (package_.getTourDate() != null) {
				preparedStatement.setDate(10, convertSqlDate(package_.getTourDate()));
			} else {
				preparedStatement.setNull(10, Types.DATE);
			}

			if (package_.getTourStatus() != null) {
				preparedStatement.setString(11, package_.getTourStatus());
			} else {
				preparedStatement.setNull(11, Types.LONGNVARCHAR);
			}

			if (package_.getTourLocation() != null) {
				preparedStatement.setString(12, package_.getTourLocation());
			} else {
				preparedStatement.setNull(12, Types.LONGNVARCHAR);
			}

			if (package_.getPreferredInvitationNumber() != null) {
				preparedStatement.setString(13, package_.getPreferredInvitationNumber());
			} else {
				preparedStatement.setNull(13, Types.LONGNVARCHAR);
			}

			if (package_.getSecondaryFirstName() != null) {
				preparedStatement.setString(14, package_.getSecondaryFirstName());
			} else {
				preparedStatement.setNull(14, Types.LONGNVARCHAR);
			}

			if (package_.getSecondaryLastName() != null) {
				preparedStatement.setString(15, package_.getSecondaryLastName());
			} else {
				preparedStatement.setNull(15, Types.LONGNVARCHAR);
			}

			if (package_.getPackageGuestType() != null) {
				preparedStatement.setString(16, package_.getPackageGuestType());
			} else {
				preparedStatement.setNull(16, Types.LONGNVARCHAR);
			}

			if (package_.getPackageCategoryType() != null) {
				preparedStatement.setString(17, package_.getPackageCategoryType());
			} else {
				preparedStatement.setNull(17, Types.LONGNVARCHAR);
			}

			if (package_.getPackageNights() != null) {
				preparedStatement.setInt(18, package_.getPackageNights());
			} else {
				preparedStatement.setNull(18, Types.INTEGER);
			}

			if (package_.getPackageStatus() != null) {
				preparedStatement.setString(19, package_.getPackageStatus());
			} else {
				preparedStatement.setNull(19, Types.LONGNVARCHAR);
			}

			if (package_.getPackageStage() != null) {
				preparedStatement.setString(20, package_.getPackageStage());
			} else {
				preparedStatement.setNull(20, Types.LONGNVARCHAR);
			}

			if (package_.getCampaign() != null) {
				preparedStatement.setString(21, package_.getCampaign());
			} else {
				preparedStatement.setNull(21, Types.LONGNVARCHAR);
			}

			if (package_.getPurchaseDate() != null) {
				preparedStatement.setDate(22, convertSqlDate(package_.getPurchaseDate()));
			} else {
				preparedStatement.setNull(22, Types.DATE);
			}

			if (package_.getExpirationDate() != null) {
				preparedStatement.setDate(23, convertSqlDate(package_.getExpirationDate()));
			} else {
				preparedStatement.setNull(23, Types.DATE);
			}

			if (package_.getArrivalDate() != null) {
				preparedStatement.setDate(24, convertSqlDate(package_.getArrivalDate()));
			} else {
				preparedStatement.setNull(24, Types.DATE);
			}

			if (package_.getDepartureDate() != null) {
				preparedStatement.setDate(25, convertSqlDate(package_.getDepartureDate()));
			} else {
				preparedStatement.setNull(25, Types.DATE);
			}

			if (package_.getCancelDate() != null) {
				preparedStatement.setDate(26, convertSqlDate(package_.getCancelDate()));
			} else {
				preparedStatement.setNull(26, Types.DATE);
			}

			if (package_.getHotelNumber() != null) {
				preparedStatement.setString(27, package_.getHotelNumber());
			} else {
				preparedStatement.setNull(27, Types.LONGNVARCHAR);
			}

			if (package_.getHotelConfirmationNumber() != null) {
				preparedStatement.setString(28, package_.getHotelConfirmationNumber());
			} else {
				preparedStatement.setNull(28, Types.LONGNVARCHAR);
			}

			if (package_.getMkgLocationCode() != null) {
				preparedStatement.setString(29, package_.getMkgLocationCode());
			} else {
				preparedStatement.setNull(29, Types.LONGNVARCHAR);
			}

			if (package_.getActiveFlag() != null) {
				preparedStatement.setBoolean(30, package_.getActiveFlag());
			} else {
				preparedStatement.setBoolean(30, true);
			}

			preparedStatement.setDate(31, convertSqlDate(package_.getOdsUpdateDtm()));
			preparedStatement.setLong(32, package_.getOdsPackageId());
			preparedStatement.setDate(33, convertSqlDate(new Date()));
			preparedStatement.setDate(34, convertSqlDate(new Date()));
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw e;
		}
	}

}
