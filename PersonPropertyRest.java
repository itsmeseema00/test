package com.vistana.onsiteconcierge.rest;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vistana.onsiteconcierge.config.Constants;
import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.AuthenticatedUserDto;
import com.vistana.onsiteconcierge.core.dto.PersonPropertyDto;
import com.vistana.onsiteconcierge.core.model.SessionState;
import com.vistana.onsiteconcierge.core.service.PersonPropertyService;
import com.vistana.onsiteconcierge.core.service.PersonService;
import com.vistana.onsiteconcierge.core.service.SessionStateService;

@RestController
@RequestMapping("/personProperty")
public class PersonPropertyRest {
	private static final Logger log = Logger.getLogger(PersonPropertyRest.class.getName());

	@Autowired
	private PersonPropertyService personPropertyService;

	@Autowired
	private SessionStateService sessionStateService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PersonService personService;

	/**
	 * Return the person properties of the user that made the request.
	 * 
	 * @param organization
	 *            The current logged in organization of the user
	 * @param property
	 *            The current four digit property number.
	 * @return A collection of the
	 */
	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@GetMapping()
	public ResponseEntity<PersonPropertyDto> getPersonProperty(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property) {

		AuthenticatedUserDto user = AuthenticatedUserDto
				.getUser(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		return new ResponseEntity<>(personPropertyService.getPersonProperty(organization, property, user.getId()),
				HttpStatus.OK);
	}

	@PreAuthorize(Constants.ALLOWED_FOR_ORGANIZATION_PROPERTY)
	@PostMapping()
	public AuthenticatedUserDto updatePersonPropertyDefaults(
			@RequestParam(value = CoreConstants.ORGANIZATION) String organization,
			@RequestParam(value = CoreConstants.PROPERTY) Integer property,
			@RequestBody PersonPropertyDto personPropertyDto) {

		AuthenticatedUserDto user = AuthenticatedUserDto
				.getUser(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		PersonPropertyDto created = personPropertyService.updatePreferences(organization, property, personPropertyDto,
				user.getId());
		List<PersonPropertyDto> pp = user.getProperties().stream().map(each -> {
			if (Objects.equals(each.getOrganization(), created.getOrganization())
					&& Objects.equals(each.getProperty(), created.getProperty())) {
				return created;
			} else {
				return each;
			}
		}).collect(Collectors.toList());
		user.setProperties(pp);
		SessionState state = sessionStateService.find(user.getToken());
		try {
			state.setPrincipal(objectMapper.writeValueAsString(user));
			sessionStateService.save(state);
		} catch (JsonProcessingException e) {
			log.log(Level.WARNING, e.toString());
		}
		return user;
	}

}
