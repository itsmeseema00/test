package com.vistana.onsiteconcierge.core.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.PropertyRepository;
import com.vistana.onsiteconcierge.core.dto.PropertyDto;
import com.vistana.onsiteconcierge.core.model.Property;
import com.vistana.onsiteconcierge.core.model.PropertyId;
import com.vistana.onsiteconcierge.core.model.Timezone;
import com.vistana.onsiteconcierge.core.model.TimezoneId;
import com.vistana.onsiteconcierge.core.service.PropertyService;
import com.vistana.onsiteconcierge.core.service.TimezoneService;

@Service
public class PropertyServiceImpl extends SaveDeleteServiceImpl<Property, PropertyId> implements PropertyService {

	@Autowired
	private PropertyRepository repository;
	@Autowired
	private TimezoneService timezoneService;

	@Override
	public Property findById(PropertyId propertyId) {

		return repository.findById(propertyId);
	}

	@Override
	public List<Property> findByIdOrganizationId(String organizationId) {

		return repository.findByIdOrganizationId(organizationId);
	}

	@Override
	public List<PropertyDto> findAllByIdOrganizationId(String organizationId) {

		return repository.findByIdOrganizationId(organizationId).stream().map((e) -> {
			PropertyDto propertyDto = new PropertyDto(e);
			if (e != null && e.getId() != null) {
				propertyDto.setOrganizationId(e.getId().getOrganizationId());
				propertyDto.setPropertyId(e.getId().getPropertyId());
			}
			return propertyDto;
		}).collect(Collectors.toList());
	}

	@Override
	public List<Property> findByInternalPropertyIdAndOrganization(String internalPropertyId, String organizationId) {

		return repository.findByInternalPropertyIdAndIdOrganizationId(internalPropertyId, organizationId);
	}

	@Override
	protected CrudRepository<Property, PropertyId> getRepository() {

		return repository;
	}

	/**
	 * Converts a date to local timezone of a given property.
	 * 
	 * @param inDate
	 *            Date to offset, assumed to be system default.
	 * @param propertyId
	 *            Property Id to correct the date.
	 * @param organizationId
	 *            Organization of the property.
	 * @return The property corrected date.
	 */
	public Date getTZCorrectedDate(Date inDate, Integer propertyId, String organizationId) {
		Property property = findById(new PropertyId(organizationId, propertyId));
		Timezone propertyTimezone = timezoneService.find(new TimezoneId(organizationId, property.getTimezoneId()));
		// Assume the incoming date is stored as the systems timezone.
		ZonedDateTime zonedDt = inDate.toInstant().atZone(ZoneId.systemDefault());
		LocalDateTime localDate = zonedDt.toLocalDateTime();
		Instant convertedInstant = localDate.atZone(ZoneId.of(propertyTimezone.getTimezoneName())).toInstant();
		return Date.from(convertedInstant);
	}

}
