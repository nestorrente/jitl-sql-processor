package com.nestorrente.jitl.module.sql.exception;

public class SyntaxErrorException extends RuntimeException {

	private static final long serialVersionUID = 8939053098408837757L;

	public SyntaxErrorException(String message) {
		super(message);
	}

}
