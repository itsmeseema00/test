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

import com.vistana.onsiteconcierge.core.dao.OwnerRepository;
import com.vistana.onsiteconcierge.core.model.CustomerUniqueId;
import com.vistana.onsiteconcierge.core.model.Owner;
import com.vistana.onsiteconcierge.core.model.OwnerContract;
import com.vistana.onsiteconcierge.core.model.OwnerId;
import com.vistana.onsiteconcierge.core.model.QOwner;
import com.vistana.onsiteconcierge.core.model.QOwnerContract;
import com.vistana.onsiteconcierge.core.service.OwnerContractService;
import com.vistana.onsiteconcierge.core.service.OwnerInventoryService;
import com.vistana.onsiteconcierge.core.service.OwnerService;

@Service
public class OwnerServiceImpl extends SaveDeleteServiceImpl<Owner, OwnerId> implements OwnerService {

	@Autowired
	protected OwnerContractService ownerContractService;

	@Autowired
	protected OwnerInventoryService ownerInventoryService;

	@Autowired
	private OwnerRepository repository;

	private java.sql.Date convertSqlDate(Date date) {

		if (date == null) {
			return null;
		} else {
			return new java.sql.Date(date.getTime());
		}
	}

	@Override
	@Transactional
	public void deleteFully(Owner owner) {

		if (owner.getOwnerContracts() != null) {
			owner.getOwnerContracts().forEach(contract -> {
				if (contract.getOwnerInventories() != null) {
					ownerInventoryService.delete(contract.getOwnerInventories());
				}
			});
			ownerContractService.delete(owner.getOwnerContracts());
		}
		getRepository().delete(owner);
	}

	@Override
	public void deleteFullyNonTrans(Owner owner) throws SQLException {

		if (owner.getOwnerContracts() != null) {
			Iterator<OwnerContract> cont = owner.getOwnerContracts().iterator();

			while (cont.hasNext()) {
				OwnerContract contract = cont.next();
				if (contract.getOwnerInventories() != null) {
					ownerInventoryService.deleteNonTrans(contract.getOwnerInventories());
				}
			}
			ownerContractService.nonTransDelete(owner.getOwnerContracts());
		}
		this.deleteNonTrans(owner);
	}

	@Override
	public void deleteNonTrans(Owner owner) throws SQLException {

		Connection connection = getEntityManager().unwrap(SessionImpl.class).connection();
		try {
			String strQuery = "DELETE from Owner WHERE " + " OrganizationId =? " // 0
					+ " AND OwnerId =? AND "// 1
					+ "SourceSystemName =?";
			PreparedStatement preparedStatement = null;
			preparedStatement = connection.prepareStatement(strQuery);
			connection.setAutoCommit(false);
			OwnerId id = owner.getId();
			preparedStatement.setString(1, id.getOrganizationId());
			preparedStatement.setString(2, id.getOwnerId());
			preparedStatement.setString(3, id.getSourceSystemName());

			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw e;
		}
	}

	@Override
	public List<Owner> findByIdOrganization(String organization, Integer customerUniqueId) {

		return repository.findByIdOrganizationIdAndCustomerUniqueId(organization, customerUniqueId);
	}

	@Override
	public List<Owner> findByIdOrganization(String organization, String ownerId) {

		return repository.findByIdOrganizationIdAndIdOwnerId(organization, ownerId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Owner> findFullyLoaded(CustomerUniqueId customerId) {

		Query query = getEntityManager().createQuery(
				"SELECT own FROM Owner own LEFT JOIN FETCH own.ownerContracts contracts LEFT JOIN FETCH contracts.ownerInventories inventories"
						+ " WHERE own.id.organizationId = :org AND own.customerUniqueId = :cust ");
		query.setParameter("org", customerId.getOrganizationId());
		query.setParameter("cust", customerId.getCustomerUniqueId());
		return query.getResultList();
	}

	@Override
	public List<Owner> getOwnerAndOwnerContractAndOwnerInventory(String organizationId, String ownerId) {

		QOwner owner = new QOwner("owner");
		QOwnerContract ownerContracts = new QOwnerContract("ownerContracts");

		return getQueryFactory().selectFrom(owner).innerJoin(owner.ownerContracts, ownerContracts).fetchJoin()
				.where(owner.id.organizationId.eq(organizationId).and(owner.id.ownerId.eq(ownerId))).fetch();
	}

	@Override
	protected CrudRepository<Owner, OwnerId> getRepository() {

		return repository;
	}

	@Override
	public void nonTransSave(Set<Owner> owners) throws SQLException {
		Iterator<Owner> own = owners.iterator();
		while (own.hasNext()) {
			this.saveNative(own.next());
		}
		;
	}

	public void saveNative(Owner owner) throws SQLException {
		Connection connection = getEntityManager().unwrap(SessionImpl.class).connection();
		try {
			String sql = "INSERT INTO Owner ( " + "Owner.OrganizationId, " + "Owner.OwnerId, "
					+ "Owner.SourceSystemName, " + "Owner.CustomerUniqueId, " + "Owner.OwnerSince, "
					+ "Owner.RCIMember," + " Owner.IIMemeber, " + "Owner.EliteLevel, " + "Owner.NumberOfContracts, "
					+ "Owner.PropertyList, " + "Owner.ActiveFlag, " + "Owner.ODSUpdateDtm, " + "Owner.ODSPackageId, "
					+ "Owner.CreateDtm, " + "Owner.UpdateDtm) " + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement preparedStatement = null;
			preparedStatement = connection.prepareStatement(sql);
			OwnerId id = owner.getId();
			preparedStatement.setString(1, id.getOrganizationId());
			preparedStatement.setString(2, id.getOwnerId());
			preparedStatement.setString(3, id.getSourceSystemName());
			preparedStatement.setInt(4, owner.getCustomerUniqueId());
			if (owner.getOwnerSince() != null) {
				preparedStatement.setDate(5, convertSqlDate(owner.getOwnerSince()));
			} else {

				preparedStatement.setNull(5, Types.DATE);
			}
			if (owner.getRciMember() != null) {
				preparedStatement.setString(6, owner.getRciMember());
			} else {
				preparedStatement.setNull(6, Types.LONGNVARCHAR);
			}

			if (owner.getIiMemeber() != null) {
				preparedStatement.setString(7, owner.getIiMemeber());
			} else {
				preparedStatement.setNull(7, Types.LONGNVARCHAR);
			}

			if (owner.getEliteLevel() != null) {
				preparedStatement.setString(8, owner.getEliteLevel());
			} else {
				preparedStatement.setNull(8, Types.LONGNVARCHAR);
			}

			if (owner.getNumberOfContracts() != null) {
				preparedStatement.setInt(9, owner.getNumberOfContracts());
			} else {
				preparedStatement.setNull(9, Types.INTEGER);
			}

			if (owner.getPropertyList() != null) {
				preparedStatement.setString(10, owner.getPropertyList());
			} else {
				preparedStatement.setNull(10, Types.LONGNVARCHAR);
			}

			if (owner.getActiveFlag() != null) {
				preparedStatement.setBoolean(11, owner.getActiveFlag());
			} else {
				preparedStatement.setBoolean(11, true);
			}

			preparedStatement.setDate(12, convertSqlDate(owner.getOdsUpdateDtm()));
			preparedStatement.setLong(13, owner.getOdsPackageId());
			preparedStatement.setDate(14, convertSqlDate(new Date()));
			preparedStatement.setDate(15, convertSqlDate(new Date()));
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			throw e;
		}
	}

}
