package crac.models.output.errors;

import java.util.HashMap;

public class MessageHandler {
	private boolean success;
	private String error;
	private HashMap<String, String> details;

	public MessageHandler(boolean success, String error, HashMap<String, String> details) {
		this.success = success;
		this.error = error;
		this.details = details;
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

	public HashMap<String, String> getDetails() {
		return details;
	}

	public void setDetails(HashMap<String, String> details) {
		this.details = details;
	}

}
