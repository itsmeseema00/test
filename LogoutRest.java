package com.vistana.onsiteconcierge.rest;

import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vistana.onsiteconcierge.core.CoreConstants;
import com.vistana.onsiteconcierge.core.dto.AuthenticatedUserDto;
import com.vistana.onsiteconcierge.core.service.SessionStateService;

@RestController
public class LogoutRest {

	private static Logger logger = LoggerFactory.getLogger(LogoutRest.class);

	@Autowired
	private Ignite ignite;

	@Autowired
	private SessionStateService sessionStateService;

	@PostMapping(value = "/logout")
	public ResponseEntity<String> logout() {

		if (ignite != null) {

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUserDto user = AuthenticatedUserDto.getUser(authentication.getPrincipal());
			sessionStateService.delete(user.getToken());
			logger.info("logout");
		}

		return new ResponseEntity<String>(CoreConstants.SUCCESSFUL, HttpStatus.OK);
	}
}
