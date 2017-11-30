package com.vistana.onsiteconcierge.core.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class PmpResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler({ InvalidClientRequest.class })
	protected ResponseEntity<Object> handleInvalidClientRequestException(RuntimeException error, WebRequest request) {

		InvalidClientRequest jdbc = (InvalidClientRequest) error;
		ErrorResponse response = new ErrorResponse(400, jdbc.getMessage());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return handleExceptionInternal(error, response, headers, HttpStatus.BAD_REQUEST, request);

	}

	@ExceptionHandler({ GenericException.class })
	protected ResponseEntity<Object> handleJdbcException(RuntimeException error, WebRequest request) {

		GenericException jdbc = (GenericException) error;
		ErrorResponse response = new ErrorResponse(500, jdbc.getMessage());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return handleExceptionInternal(error, response, headers, HttpStatus.INTERNAL_SERVER_ERROR, request);

	}
}
