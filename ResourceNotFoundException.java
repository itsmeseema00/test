package com.vistana.onsiteconcierge.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "404 Requested Resource does not exist.")
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -5543046386564259874L;

}
