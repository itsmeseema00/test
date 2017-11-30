package com.vistana.onsiteconcierge.core.exception;

public class InvalidClientRequest extends RuntimeException {

	private static final long serialVersionUID = -5543046386564259874L;

	public InvalidClientRequest() {
	}

	public InvalidClientRequest(String detailMessage) {
		super(detailMessage);
	}

}
