package crac.models.output.errors;

import crac.enums.ErrorCause;

public class RESTError {
	
	private ErrorCause name;
	private String cause;
	
	
	
	public RESTError(ErrorCause name, String cause) {
		this.name = name;
		this.cause = cause;
	}
	
	public ErrorCause getName() {
		return name;
	}
	public String getCause() {
		return cause;
	}
	
}
