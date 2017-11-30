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

import com.vistana.onsiteconcierge.core.dao.OwnerInventoryRepository;
import com.vistana.onsiteconcierge.core.model.OwnerInventory;
import com.vistana.onsiteconcierge.core.model.OwnerInventoryId;
import com.vistana.onsiteconcierge.core.service.OwnerInventoryService;

@Service
public class OwnerInventoryServiceImpl extends SaveDeleteServiceImpl<OwnerInventory, OwnerInventoryId>
		implements OwnerInventoryService {

	@Autowired
	private OwnerInventoryRepository repository;

	private java.sql.Date convertSqlDate(Date date) {

		if (date == null) {
			return null;
		} else {
			return new java.sql.Date(date.getTime());
		}
	}

	@Override
	public void deleteNonTrans(Set<OwnerInventory> inventories) throws SQLException {
		if (inventories.size() > 0) {
			Connection connection = getEntityManager().unwrap(SessionImpl.class).connection();
			try {
				String strQuery = "DELETE from OwnerInventory WHERE " + " OrganizationId =? " // 1
						+ " AND OwnerId =? AND "// 2
						+ "SourceSystemName =?";// 3
				inventories.forEach((inv) -> {
					this.getEntityManager().detach(inv);
				});
				PreparedStatement preparedStatement = null;
				preparedStatement = connection.prepareStatement(strQuery);
				connection.setAutoCommit(false);
				OwnerInventory inv = inventories.iterator().next();
				OwnerInventoryId id = inv.getId();
				preparedStatement.setString(1, id.getOrganizationId());
				preparedStatement.setString(2, id.getOwnerId());
				preparedStatement.setString(3, id.getSourceSystemName());
				preparedStatement.executeUpdate();
			} catch (SQLException e) {
				throw e;
			}
		}

	}

	@Override
	public List<OwnerInventory> findByIdOrganization(String organization, String ownerId) {

		return repository.findByIdOrganizationIdAndIdOwnerId(organization, ownerId);
	}

	@Override
	protected CrudRepository<OwnerInventory, OwnerInventoryId> getRepository() {

		return repository;
	}

	@Override
	public void nonTransSave(List<OwnerInventory> inventories) throws SQLException {
		Iterator<OwnerInventory> inv = inventories.iterator();
		while (inv.hasNext()) {
			this.saveNative(inv.next());
		}
	}

	private void saveNative(OwnerInventory inventory) throws SQLException {
		Connection connection = getEntityManager().unwrap(SessionImpl.class).connection();
		String sql = "INSERT INTO OwnerInventory ( " + "OwnerInventory.OrganizationId, " + "OwnerInventory.OwnerId,"
				+ " OwnerInventory.SourceSystemName, " + "OwnerInventory.OwnerType, "
				+ "OwnerInventory.InternalPropertyId," + " OwnerInventory.Company, " + "OwnerInventory.ContractNumber,"
				+ "OwnerInventory.Unit," + "OwnerInventory.Week," + "OwnerInventory.ProductType,"
				+ "OwnerInventory.ProductDescription," + "OwnerInventory.OnDate," + "OwnerInventory.FirstOccurrence,"
				+ "OwnerInventory.Club," + "OwnerInventory.SVNUnitSize," + "OwnerInventory.PrivacyOcc,"
				+ "OwnerInventory.MaximumOcc," + "OwnerInventory.CondoCompany," + "OwnerInventory.OwnerOptions,"
				+ "OwnerInventory.OwnerPoints," + "OwnerInventory.CondoStatus," + "OwnerInventory.CondoLockout,"
				+ "OwnerInventory.PointsPurchased," + "OwnerInventory.ProductClass," + "OwnerInventory.ActiveFlag,"
				+ "OwnerInventory.ODSUpdateDtm," + "OwnerInventory.ODSPackageId," + "OwnerInventory.CreateDtm,"
				+ "OwnerInventory.UpdateDtm)" + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {

			connection.setAutoCommit(false);
			PreparedStatement preparedStatement = null;
			preparedStatement = connection.prepareStatement(sql);

			OwnerInventoryId id = inventory.getId();
			preparedStatement.setString(1, id.getOrganizationId());
			preparedStatement.setString(2, id.getOwnerId());
			preparedStatement.setString(3, id.getSourceSystemName());
			preparedStatement.setString(4, id.getOwnerType());
			preparedStatement.setString(5, id.getInternalPropertyId());
			preparedStatement.setString(6, id.getCompany());
			preparedStatement.setString(7, id.getContractNumber());
			preparedStatement.setString(8, id.getUnit());
			preparedStatement.setString(9, id.getWeek());
			preparedStatement.setString(10, id.getProductType());

			if (inventory.getProductDescription() != null) {
				preparedStatement.setString(11, inventory.getProductDescription());
			} else {
				preparedStatement.setNull(11, Types.LONGNVARCHAR);
			}

			if (inventory.getOnDate() != null) {
				preparedStatement.setDate(12, convertSqlDate(inventory.getOnDate()));
			} else {
				preparedStatement.setNull(12, Types.DATE);
			}

			if (inventory.getFirstOccurence() != null) {
				preparedStatement.setDate(13, convertSqlDate(inventory.getFirstOccurence()));
			} else {
				preparedStatement.setNull(13, Types.DATE);
			}

			if (inventory.getClub() != null) {
				preparedStatement.setString(14, inventory.getClub());
			} else {
				preparedStatement.setNull(14, Types.LONGNVARCHAR);
			}

			if (inventory.getSvnUnitSize() != null) {
				preparedStatement.setString(15, inventory.getSvnUnitSize());
			} else {
				preparedStatement.setNull(15, Types.LONGNVARCHAR);
			}

			if (inventory.getPrivacyOcc() != null) {
				preparedStatement.setString(16, inventory.getPrivacyOcc());
			} else {
				preparedStatement.setNull(16, Types.LONGNVARCHAR);
			}

			if (inventory.getMaximumOcc() != null) {
				preparedStatement.setString(17, inventory.getMaximumOcc());
			} else {
				preparedStatement.setNull(17, Types.LONGNVARCHAR);
			}

			if (inventory.getCondocompany() != null) {
				preparedStatement.setString(18, inventory.getCondocompany());
			} else {
				preparedStatement.setNull(18, Types.LONGNVARCHAR);
			}

			if (inventory.getOwnerOptions() != null) {
				preparedStatement.setString(19, inventory.getOwnerOptions());
			} else {
				preparedStatement.setNull(19, Types.LONGNVARCHAR);
			}

			if (inventory.getOwnerPoints() != null) {
				preparedStatement.setString(20, inventory.getOwnerPoints());
			} else {
				preparedStatement.setNull(20, Types.LONGNVARCHAR);
			}

			if (inventory.getCondoStatus() != null) {
				preparedStatement.setString(21, inventory.getCondoStatus());
			} else {
				preparedStatement.setNull(21, Types.LONGNVARCHAR);
			}

			if (inventory.getCondoLockout() != null) {
				preparedStatement.setString(22, inventory.getCondoLockout());
			} else {
				preparedStatement.setNull(22, Types.LONGNVARCHAR);
			}

			if (inventory.getPointsPurchased() != null) {
				preparedStatement.setBigDecimal(23, inventory.getPointsPurchased());
			} else {
				preparedStatement.setNull(23, Types.DECIMAL);
			}

			if (inventory.getProductClass() != null) {
				preparedStatement.setString(24, inventory.getProductClass());
			} else {
				preparedStatement.setNull(24, Types.LONGNVARCHAR);
			}

			if (inventory.getActiveFlag() != null) {
				preparedStatement.setBoolean(25, inventory.getActiveFlag());
			} else {
				preparedStatement.setBoolean(25, true);
			}

			preparedStatement.setDate(26, convertSqlDate(inventory.getOdsUpdateDtm()));
			preparedStatement.setLong(27, inventory.getOdsPackageId());
			preparedStatement.setDate(28, convertSqlDate(new Date()));
			preparedStatement.setDate(29, convertSqlDate(new Date()));
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			throw e;
		}
	}

}