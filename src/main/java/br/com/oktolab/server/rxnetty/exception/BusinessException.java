package br.com.oktolab.server.rxnetty.exception;

import com.google.gson.JsonObject;

public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 7448660762447357176L;
	private static final String MESSAGE_ATTR = "message";
	
	private Object value;

	public BusinessException() {
	}

	public BusinessException(String message) {
		super(message);
		this.value = new JsonObject();
		((JsonObject)this.value).addProperty(MESSAGE_ATTR, message);
	}
	
	public BusinessException(Object value) {
		super();
		this.value = value;
	}
	
	public BusinessException(String message, Object value, Throwable exception) {
		super(message, exception);
		this.value = value;
	}
	
	public BusinessException(String message, Throwable exception) {
		super(message, exception);
		this.value = new JsonObject();
		((JsonObject)this.value).addProperty(MESSAGE_ATTR, message);
	}

	public BusinessException(Object value, Throwable exception) {
		super();
		this.value = value;
	}
	
	public BusinessException(Throwable exception) {
		super(exception);
	}
	
	protected BusinessException(String message, Throwable exception, boolean enableSuppression, boolean writableStackTrace) {
		super(message, exception, enableSuppression, writableStackTrace);
		this.value = new JsonObject();
		((JsonObject)this.value).addProperty(MESSAGE_ATTR, message);
	}
	
	public Object getValue() {
		return value;
	}

}
