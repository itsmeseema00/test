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

import com.vistana.onsiteconcierge.core.dao.OwnerContractRepository;
import com.vistana.onsiteconcierge.core.model.OwnerContract;
import com.vistana.onsiteconcierge.core.model.OwnerContractId;
import com.vistana.onsiteconcierge.core.service.OwnerContractService;

@Service
public class OwnerContractServiceImpl extends SaveDeleteServiceImpl<OwnerContract, OwnerContractId>
		implements OwnerContractService {

	@Autowired
	private OwnerContractRepository repository;

	private java.sql.Date convertSqlDate(Date date) {

		if (date == null) {
			return null;
		} else {
			return new java.sql.Date(date.getTime());
		}
	}

	@Override
	public List<OwnerContract> findByIdOrganization(String organization, String ownerId) {

		return repository.findByIdOrganizationIdAndIdOwnerId(organization, ownerId);
	}

	@Override
	protected CrudRepository<OwnerContract, OwnerContractId> getRepository() {

		return repository;
	}

	@Override
	public void nonTransDelete(Set<OwnerContract> contracts) throws SQLException {
		Connection connection = getEntityManager().unwrap(SessionImpl.class).connection();
		try {
			String strQuery = "DELETE from OwnerContract WHERE " + " OrganizationId =? " // 1
					+ " AND OwnerId =? "; // 2

			PreparedStatement preparedStatement = null;
			preparedStatement = connection.prepareStatement(strQuery);
			connection.setAutoCommit(false);
			OwnerContract cnt = contracts.iterator().next();
			OwnerContractId id = cnt.getId();
			preparedStatement.setString(1, id.getOrganizationId());
			preparedStatement.setString(2, id.getOwnerId());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw e;
		}
	}

	@Override
	public void nonTransSave(List<OwnerContract> owners) throws SQLException {
		Iterator<OwnerContract> contracts = owners.iterator();
		while (contracts.hasNext()) {
			this.saveNative(contracts.next());
		}
	}

	private void saveNative(OwnerContract contract) throws SQLException {
		Connection connection = getEntityManager().unwrap(SessionImpl.class).connection();
		String sql = "INSERT INTO OwnerContract (" + "OwnerContract.OrganizationId, " + "OwnerContract.OwnerId, "
				+ "OwnerContract.SourceSystemName, " + "OwnerContract.OwnerType, "
				+ "OwnerContract.InternalPropertyId, " + "OwnerContract.Company, " + "OwnerContract.ContractNumber, "
				+ "OwnerContract.Resale, " + "OwnerContract.EliteLevel, " + "OwnerContract.ActiveFlag, "
				+ "OwnerContract.ODSUpdateDtm, " + "OwnerContract.ODSPackageId, " + "OwnerContract.CreateDtm, "
				+ "OwnerContract.UpdateDtm) " + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		PreparedStatement preparedStatement = null;
		preparedStatement = connection.prepareStatement(sql);
		connection.setAutoCommit(false);
		OwnerContractId id = contract.getId();
		preparedStatement.setString(1, id.getOrganizationId());
		preparedStatement.setString(2, id.getOwnerId());
		preparedStatement.setString(3, id.getSourceSystemName());
		preparedStatement.setString(4, id.getOwnertype());
		preparedStatement.setString(5, id.getInternalPropertyId());
		preparedStatement.setString(6, id.getCompany());
		preparedStatement.setString(7, id.getContractNumber());
		if (contract.getResale() != null) {
			preparedStatement.setString(8, contract.getResale());
		} else {

			preparedStatement.setNull(8, Types.CHAR);
		}

		if (contract.getEliteLevel() != null) {
			preparedStatement.setString(9, contract.getEliteLevel());
		} else {
			preparedStatement.setNull(9, Types.LONGNVARCHAR);
		}

		if (contract.getActiveFlag() != null) {
			preparedStatement.setBoolean(10, contract.getActiveFlag());
		} else {
			preparedStatement.setBoolean(10, true);
		}

		preparedStatement.setDate(11, convertSqlDate(contract.getOdsUpdateDtm()));
		preparedStatement.setLong(12, contract.getOdsPackageId());
		preparedStatement.setDate(13, convertSqlDate(new Date()));
		preparedStatement.setDate(14, convertSqlDate(new Date()));
		preparedStatement.executeUpdate();
	}

}