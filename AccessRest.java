package com.vistana.onsiteconcierge.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.AccessDto;
import com.vistana.onsiteconcierge.core.model.Access;
import com.vistana.onsiteconcierge.core.service.AccessService;

@RestController
@RequestMapping("/access")
public class AccessRest {

	@Autowired
	private AccessService service;

	@GetMapping(value = "/accesses")
	public List<AccessDto> getAccesses(@RequestParam(value = CoreConstants.ORGANIZATION) String organization) {

		List<Access> accesses = service.findByOrganization(organization);
		return accesses.stream().map(item -> new AccessDto(item)).collect(Collectors.toList());
	}

}
