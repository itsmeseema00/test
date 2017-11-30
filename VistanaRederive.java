package com.vistana.onsiteconcierge.core.service.impl;

import static com.vistana.onsiteconcierge.core.CoreConstants.GUEST_TYPE_OWNER_LEGACY;
import static com.vistana.onsiteconcierge.core.CoreConstants.GUEST_TYPE_OWNER_OWNER;
import static com.vistana.onsiteconcierge.core.CoreConstants.GUEST_TYPE_OWNER_OWNER_SVN;
import static com.vistana.onsiteconcierge.core.CoreConstants.GUEST_TYPE_OWNER_POINTS;
import static com.vistana.onsiteconcierge.core.CoreConstants.GUEST_TYPE_OWNER_RESALE;
import static com.vistana.onsiteconcierge.core.CoreConstants.GUEST_TYPE_UNKNOWN;

import java.util.ArrayList;
import java.util.List;

import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.model.Rate;
import com.vistana.onsiteconcierge.core.service.LookupService;

// https://vistana.attask-ondemand.com/task/view?ID=57c8436902bb1be7586ef85069a3d15f
public class VistanaRederive {

	private static final Boolean FALSE = false;
	private static final String REMARK = "Applied Rederive";

	private List<LeadContact> leads;

	private String organizationId;

	private Integer propertyId;

	private List<Rate> rates;

	public VistanaRederive(String organizationId, Integer propertyId, List<Rate> rates, List<LeadContact> leads) {
		this.organizationId = organizationId;
		this.propertyId = propertyId;
		this.rates = rates;
		this.leads = leads;
	}

	public List<LeadContact> applyRules() {

		List<LeadContact> updated = new ArrayList<>();
		leads.forEach(lead -> {
			Rate rateToFind = new Rate(organizationId, propertyId, lead.getStay().getStayRateCode());
			if (rates.contains(rateToFind)) {
				Rate rate = rates.get(rates.indexOf(rateToFind));

				if (LookupService.GUEST_TYPE_ROOM_SHARER.equals(lead.getGuestTypeCode())) {
					// do not rederive Room Sharer
					// 4b. Given the Guest Type is 'RS', then should not rederive.
				} else if (lead.getOverrideGuestTypeFlag() != null && lead.getOverrideGuestTypeFlag()) {
					// do not override manually set guest type
					// 4a. Given the Guest Type Override flag is true, then should not rederive.
				} else if (rate.isOverride()) {
					/*
					 * Given a lead has Preview Package, Explorer Package, or Owner information
					 * returned from the ODS and the lead's guest type to rate code mapping has an
					 * Override value of "YES", the lead's guest type is set to the option mapped
					 * for the lead's rate code.
					 */

					// override flag overrides all
					updateGuestType(updated, lead, rate.getGuestTypeCode());
				} else if (lead.isOwnerActive()) {
					/*
					 * 8. Given a lead has Owner information returned from the ODS, no Preview
					 * Package information was returned, no Explorer Package information was
					 * returned, and the lead's guest type to rate code mapping has an Override
					 * value of "No", the lead's guest type is set to one of the following Owner
					 * guest types.
					 */
					if (lead.isResale()) {
						updateGuestType(updated, lead, GUEST_TYPE_OWNER_RESALE);
					} else if (lead.isLegacy()) {
						updateGuestType(updated, lead, GUEST_TYPE_OWNER_LEGACY);
					} else if (lead.isPoints()) {
						updateGuestType(updated, lead, GUEST_TYPE_OWNER_POINTS);
					} else if (lead.isOwner()) {
						updateGuestType(updated, lead, GUEST_TYPE_OWNER_OWNER);
					} else {
						updateGuestType(updated, lead, GUEST_TYPE_OWNER_OWNER_SVN);
					}
				} else {
					updateGuestType(updated, lead, rate.getGuestTypeCode());
				}
			} else {
				updateGuestType(updated, lead, GUEST_TYPE_UNKNOWN);
			}
		});
		return updated;
	}

	private void updateGuestType(List<LeadContact> updated, LeadContact lead, String guestType) {

		if (!guestType.equals(lead.getGuestTypeCode())) {
			lead.setRemarksText(REMARK, FALSE);
			lead.setGuestTypeCode(guestType);
			updated.add(lead);
		}
	}

}
