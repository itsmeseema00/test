package com.vistana.onsiteconcierge.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.vistana.onsiteconcierge.core.dto.ActivatorDto;

@Entity(name = "Activator")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Activator extends AbstractTimestampEntity<Activator, Integer> implements Serializable, IRankable {

	private static final long serialVersionUID = 3106201514213368945L;

	@Column(name = "ActivatorDesc", columnDefinition = "VARCHAR", length = 100, nullable = false)
	private String activatorDesc;

	@Id
	@Column(name = "ActivatorId")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer activatorId;

	@Column(name = "ActivatorTypeCode", columnDefinition = "VARCHAR", nullable = false, length = 30)
	private String activatorTypeCode;

	@Column(name = "ActiveFlag", columnDefinition = "BIT", nullable = false)
	private Boolean activeFlag;

	@Column(name = "DefaultGiftId", columnDefinition = "INT")
	private Integer defaultGiftId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DefaultGiftId", referencedColumnName = "GiftId", insertable = false, updatable = false)
	private Gift gift;

	@Column(name = "OrganizationId", columnDefinition = "CHAR", length = 3, nullable = false)
	private String organizationId;

	@Column(name = "PropertyId", columnDefinition = "INT", nullable = false)
	private Integer propertyId;

	@Column(name = "SortOrder", columnDefinition = "INT", nullable = false)
	private Integer sortOrder;

	public Activator() {
		// default constructor
	}

	public Activator(ActivatorDto dto) {
		activeFlag = dto.getActiveFlag();
		activatorDesc = dto.getActivatorDesc();
		activatorTypeCode = dto.getActivatorTypeCode();
		sortOrder = dto.getSortOrder();
		defaultGiftId = dto.getDefaultGiftId();
		activatorId = dto.getActivatorId();
	}

	public Activator(Boolean activeFlag, String activatorDesc, String activatorTypeCode, String organizationId,
			Integer propertyId, Integer defaultGiftId, Integer sortOrder, Integer activatorId) {
		this.activeFlag = activeFlag;
		this.activatorDesc = activatorDesc;
		this.activatorTypeCode = activatorTypeCode;
		this.organizationId = organizationId;
		this.propertyId = propertyId;
		this.defaultGiftId = defaultGiftId;
		this.sortOrder = sortOrder;
		this.activatorId = activatorId;
	}

	@Override
	public boolean equals(Object other) {

		if (this == other) {
			return true;
		} else if (!(other instanceof Activator)) {
			return false;
		}

		final Activator entity = (Activator) other;
		if (getId() == null || entity.getId() == null) {
			return false;
		}

		return getId().equals(entity.getId());
	}

	public String getActivatorDesc() {

		return activatorDesc;
	}

	public Integer getActivatorId() {

		return activatorId;
	}

	public String getActivatorTypeCode() {

		return activatorTypeCode;
	}

	public Boolean getActiveFlag() {

		return activeFlag;
	}

	public Integer getDefaultGiftId() {

		return defaultGiftId;
	}

	public Gift getGift() {

		return gift;
	}

	@Override
	public Integer getId() {

		return activatorId;
	}

	public String getOrganizationId() {

		return organizationId;
	}

	public Integer getPropertyId() {

		return propertyId;
	}

	@Override
	public Integer getSortOrder() {

		return sortOrder;
	}

	@Override
	public int hashCode() {

		if (activatorId != null) {
			return activatorId.hashCode();
		} else {
			return super.hashCode();
		}
	}

	public void setActivatorDesc(String activatorDesc) {

		this.activatorDesc = activatorDesc;
	}

	public void setActivatorId(Integer activatorId) {

		this.activatorId = activatorId;
	}

	public void setActivatorTypeCode(String activatorTypeCode) {

		this.activatorTypeCode = activatorTypeCode;
	}

	public void setActiveFlag(Boolean activeFlag) {

		this.activeFlag = activeFlag;
	}

	public void setDefaultGiftId(Integer defaultGiftId) {

		this.defaultGiftId = defaultGiftId;
	}

	public void setGift(Gift gift) {

		this.gift = gift;
	}

	public void setOrganizationId(String organizationId) {

		this.organizationId = organizationId;
	}

	public void setPropertyId(Integer propertyId) {

		this.propertyId = propertyId;
	}

	@Override
	public void setSortOrder(Integer sortOrder) {

		this.sortOrder = sortOrder;
	}

	@Override
	public String toString() {

		return String.format(
				"Activator[id='%d', activatordesc='%s', activatorTypeCode='%s', defaultGiftId='%s', activeFlag'%s', sortOrder='%s']",
				activatorId, activatorDesc, activatorTypeCode, defaultGiftId, activeFlag, sortOrder);
	}

}
