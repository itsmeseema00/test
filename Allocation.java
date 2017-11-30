package com.vistana.onsiteconcierge.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity(name = "Allocation")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Allocation extends AbstractTimestampEntity<Allocation, AllocationId> implements Serializable {

	private static final long serialVersionUID = 2963863728842673716L;

	@Column(name = "ActiveFlag", columnDefinition = "BIT", length = 1, nullable = false)
	private Boolean activeFlag;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "AllocationCategoryCode", referencedColumnName = "AllocationCategoryCode", insertable = false, updatable = false),
			@JoinColumn(name = "OrganizationId", columnDefinition = "char", referencedColumnName = "OrganizationId", insertable = false, updatable = false) })
	private AllocationCategory allocationCategory;

	@Column(name = "AllocationName", nullable = false, length = 100)
	private String allocationName;

	@EmbeddedId
	private AllocationId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "OrganizationId", referencedColumnName = "OrganizationId", insertable = false, updatable = false)
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "PropertyId", referencedColumnName = "PropertyId", insertable = false, updatable = false),
			@JoinColumn(name = "OrganizationId", referencedColumnName = "OrganizationId", insertable = false, updatable = false) })
	private Property property;

	protected Allocation() {
		// default constructor
	}

	public Allocation(AllocationId id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object other) {

		if (this == other) {
			return true;
		} else if (!(other instanceof Allocation)) {
			return false;
		}

		final Allocation entity = (Allocation) other;
		if (getId() == null || entity.getId() == null) {
			return false;
		}

		return getId().equals(entity.getId());
	}

	public Boolean getActiveFlag() {

		return activeFlag;
	}

	public AllocationCategory getAllocationCategory() {

		return allocationCategory;
	}

	public String getAllocationName() {

		return allocationName;
	}

	@Override
	public AllocationId getId() {

		return id;
	}

	public Organization getOrganization() {

		return organization;
	}

	public Property getProperty() {

		return property;
	}

	public void setActiveFlag(Boolean activeFlag) {

		this.activeFlag = activeFlag;
	}

	public void setAllocationCategory(AllocationCategory allocationCategory) {

		this.allocationCategory = allocationCategory;
	}

	public void setAllocationName(String allocationName) {

		this.allocationName = allocationName;
	}

	public void setId(AllocationId id) {

		this.id = id;
	}

	public void setOrganization(Organization organization) {

		this.organization = organization;
	}

	public void setProperty(Property property) {

		this.property = property;
	}

	@Override
	public String toString() {

		return String.format("Allocation[Id='%s', allocationName='%s', activeFlag='%b']", id, allocationName,
				activeFlag);
	}

}
