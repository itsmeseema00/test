package com.vistana.onsiteconcierge.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "403 Resource Forbidden.")
public class SecurityAccessException extends RuntimeException {

	private static final long serialVersionUID = 8431656570250128606L;

}
