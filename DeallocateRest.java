package com.vistana.onsiteconcierge.rest;

import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.LeadContactDto;
import com.vistana.onsiteconcierge.core.exception.InvalidClientRequest;
import com.vistana.onsiteconcierge.core.model.GuestId;
import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.service.LeadContactService;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeallocateRest {

	@Autowired
	private LeadContactService service;

	@PostMapping("deallocate")
	public Boolean deallocate(@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property, @RequestBody List<LeadContactDto> dto) {

		List<GuestId> guestIds = dto.stream().map((lead) -> {
			if (lead.getAllocatedPersonId() == null) {
				throw new InvalidClientRequest();
			}
			return new GuestId(organization, property, lead.getResConfirmation(), lead.getRoomSeq(),
					lead.getGuestSeq());
		}).collect(Collectors.toList());

		Optional<LeadContactDto> earliest = dto.stream().min(Comparator.comparing((lead) -> {
			return lead.getArrivalDate();
		}));

		Optional<LeadContactDto> latest = dto.stream().max(Comparator.comparing((lead) -> {
			return lead.getArrivalDate();
		}));

		// Getting a collection of all possible leads
		List<LeadContact> leads = service.findByArrivalDeparture(organization, property, earliest.get().getArrivalDate(),
			latest.get().getArrivalDate(), false);

		List<LeadContact> contacts = leads.stream().filter((lead) -> {
			return guestIds.contains(lead.getId());
		}).collect(Collectors.toList());

		contacts.forEach((lead) -> {
			lead.setOwners(null);
			lead.setPackages(null);
			lead.setLinkedTours(null);
		});

		while (contacts.size() > 0) {
			List<LeadContact> batch = shrinkTo(contacts);
			service.deallocateLeads(batch);
			contacts.removeAll(batch);
		}

		return true;
	}

	private List<LeadContact> shrinkTo(List<LeadContact> ids) {

		int batchSize = 1200;
		if (ids.size() > batchSize) {
			return ids.subList(0, batchSize);
		}
		return ids;

	}

}
