package com.vistana.onsiteconcierge.core.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.internal.SessionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.PackageGiftRepository;
import com.vistana.onsiteconcierge.core.model.PackageGift;
import com.vistana.onsiteconcierge.core.model.PackageGiftId;
import com.vistana.onsiteconcierge.core.service.PackageGiftService;

@Service
public class PackageGiftServiceImpl extends SaveDeleteServiceImpl<PackageGift, PackageGiftId>
		implements PackageGiftService {

	@Autowired
	private PackageGiftRepository repository;

	private java.sql.Date convertSqlDate(Date date) {

		if (date == null) {
			return null;
		} else {
			return new java.sql.Date(date.getTime());
		}
	}

	@Override
	protected CrudRepository<PackageGift, PackageGiftId> getRepository() {

		return repository;
	}

	@Override
	public void nonTransDelete(Set<PackageGift> packageGifts) throws SQLException {
		if (packageGifts.size() > 0) {
			packageGifts.forEach((gift) -> {
				this.getEntityManager().detach(gift);
			});
			Connection connection = getEntityManager().unwrap(SessionImpl.class).connection();
			try {
				String strQuery = "DELETE from PackageGift WHERE " + " OrganizationId =? " // 0
						+ " AND PackageId =? AND "// 1
						+ "SourceSystemName =? ";// 2
				PreparedStatement preparedStatement = null;
				preparedStatement = connection.prepareStatement(strQuery);
				connection.setAutoCommit(false);
				PackageGift gift = packageGifts.iterator().next();
				PackageGiftId id = gift.getId();
				preparedStatement.setString(1, id.getOrganizationId());
				preparedStatement.setString(2, id.getPackageId());
				preparedStatement.setString(3, id.getSourceSystemName());
				preparedStatement.executeUpdate();
			} catch (SQLException e) {
				throw e;
			}
		}
	}

	@Override
	public void nonTransSave(List<PackageGift> gift) throws SQLException {
		Iterator<PackageGift> g = gift.iterator();
		while (g.hasNext()) {
			this.saveNative(g.next());
		}
	}

	private void saveNative(PackageGift gift) throws SQLException {

		Connection connection = getEntityManager().unwrap(SessionImpl.class).connection();
		String sql = "INSERT into PackageGift (" + " PackageGift.OrganizationId, " + "PackageGift.PackageId, "
				+ "PackageGift.SourceSystemName," + "PackageGift.PackageGiftId," + "PackageGift.GiftNumber,"
				+ "PackageGift.InternalPropertyId," + "PackageGift.PackageNumber,"
				+ "PackageGift.GiftInternalPropertyId," + "PackageGift.GiftCode," + "PackageGift.GiftName,"
				+ "PackageGift.GiftDescription," + "PackageGift.Status," + "PackageGift.CreatedDtm,"
				+ "PackageGift.ActiveFlag," + "PackageGift.ODSUpdateDtm, "
				+ "PackageGift.ODSPackageId) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			connection.setAutoCommit(false);
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			PackageGiftId id = gift.getId();
			preparedStatement.setString(1, id.getOrganizationId());
			preparedStatement.setString(2, id.getPackageId());
			preparedStatement.setString(3, id.getSourceSystemName());
			preparedStatement.setString(4, id.getPackageGiftId());

			if (gift.getGiftNumber() != null) {
				preparedStatement.setInt(5, gift.getGiftNumber());
			} else {
				preparedStatement.setNull(5, Types.INTEGER);
			}

			if (gift.getInternalPropertyId() != null) {
				preparedStatement.setString(6, gift.getInternalPropertyId());
			} else {
				preparedStatement.setNull(6, Types.LONGNVARCHAR);
			}

			if (gift.getPackageNumber() != null) {
				preparedStatement.setString(7, gift.getPackageNumber());
			} else {
				preparedStatement.setNull(7, Types.LONGNVARCHAR);
			}

			if (gift.getGiftInternalPropertyId() != null) {
				preparedStatement.setString(8, gift.getGiftInternalPropertyId());
			} else {
				preparedStatement.setNull(8, Types.LONGNVARCHAR);
			}

			if (gift.getGiftCode() != null) {
				preparedStatement.setString(9, gift.getGiftCode());
			} else {
				preparedStatement.setNull(9, Types.LONGNVARCHAR);
			}

			if (gift.getGiftName() != null) {
				preparedStatement.setString(10, gift.getGiftName());
			} else {
				preparedStatement.setNull(10, Types.LONGNVARCHAR);
			}

			if (gift.getGiftDescription() != null) {
				preparedStatement.setString(11, gift.getGiftDescription());
			} else {
				preparedStatement.setNull(11, Types.LONGNVARCHAR);
			}

			if (gift.getStatus() != null) {
				preparedStatement.setString(12, gift.getStatus());
			} else {
				preparedStatement.setNull(12, Types.LONGNVARCHAR);
			}

			preparedStatement.setDate(13, convertSqlDate(new Date()));

			if (gift.getActiveFlag() != null) {
				preparedStatement.setBoolean(14, gift.getActiveFlag());
			} else {
				preparedStatement.setBoolean(14, true);
			}

			preparedStatement.setDate(15, convertSqlDate(gift.getOdsUpdateDtm()));
			preparedStatement.setLong(16, gift.getOdsPackageId());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw e;
		}
	}

}
