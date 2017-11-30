package com.vistana.onsiteconcierge.rest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.GuestDto;
import com.vistana.onsiteconcierge.core.dto.GuestSearchDto;
import com.vistana.onsiteconcierge.core.model.Guest;
import com.vistana.onsiteconcierge.core.service.GuestService;

@RestController
@RequestMapping("/guest")
public class GuestRest {

	@Autowired
	private GuestService guestService;

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping(value = "/search")
	public List<GuestDto> getAllActivatorValues(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property,
			@RequestBody GuestSearchDto guestSearchDto) {

		Optional<List<Guest>> searchResults =  guestService.search(guestSearchDto, property, organization);
		if(searchResults.isPresent()){
			return searchResults.get().stream().map(GuestDto::new).collect(Collectors.toList());
		}
		return null;
	}
}
