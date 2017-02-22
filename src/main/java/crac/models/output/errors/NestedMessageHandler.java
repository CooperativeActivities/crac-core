package crac.models.output.errors;

import java.util.HashMap;

public class NestedMessageHandler {
	private boolean success;
	private HashMap<String, HashMap<String, String>> details;

	public NestedMessageHandler(boolean success, HashMap<String, HashMap<String, String>> details) {
		this.success = success;
		this.details = details;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public HashMap<String, HashMap<String, String>> getDetails() {
		return details;
	}

	public void setDetails(HashMap<String, HashMap<String, String>> details) {
		this.details = details;
	}

}
