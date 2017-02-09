package crac.models.output.errors;

import java.util.HashMap;

public class MessageHandler {
	private boolean success;
	private String error;
	private HashMap<String, String> cause;

	public MessageHandler(boolean success, String error, HashMap<String, String> cause) {
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

	public HashMap<String, String> getCause() {
		return cause;
	}

	public void setCause(HashMap<String, String> cause) {
		this.cause = cause;
	}

}
