package com.vistana.onsiteconcierge.core.exception;

public class InvalidEmailException extends RuntimeException {

	private static final long serialVersionUID = -5543046386564259874L;

	public InvalidEmailException() {
	}

	public InvalidEmailException(String detailMessage) {
		super(detailMessage);
	}

}
