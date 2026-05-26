package org.example.sc.models;


public class ApiException extends  RuntimeException {
	public int statusCode;
	public String errorCode;

	public ApiException(int statusCode, String errorCode, String message) {
		super(message);
		this.statusCode = statusCode;
		this.errorCode = errorCode;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getErrorCode() {
		return errorCode;
	}
}
