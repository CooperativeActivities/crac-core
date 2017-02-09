package crac.models.output.errors;

import java.util.HashMap;

public class ResponseHandler {
	
	private boolean success;
	private String error;
	private String cause;
		
	public ResponseHandler(boolean success, String error, String cause) {
		this.success = success;
		this.error = error;
		this.cause = cause;
	}
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getCause() {
		return cause;
	}
	public void setCause(String cause) {
		this.cause = cause;
	}
	
}
