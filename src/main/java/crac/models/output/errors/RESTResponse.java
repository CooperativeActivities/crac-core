package crac.models.output.errors;

import java.util.ArrayList;
import java.util.HashMap;

import crac.enums.ErrorCause;
import crac.enums.RESTAction;

public class RESTResponse<T> {
	
	private String type;
	private RESTAction action;
	private boolean success;
	private ArrayList<RESTError> errors;
	private T object;
	private HashMap<String, String> meta;
	
	public RESTResponse(RESTAction action, boolean success, T object) {
		type = object.getClass().getSimpleName();
		this.action = action;
		this.success = success;
		this.errors = new ArrayList<>();
		this.object = object;
		this.meta = new HashMap<>();
	}
	
	public void addError(ErrorCause name, String cause){
		errors.add(new RESTError(name, cause));
	}

	public String getType() {
		return type;
	}

	public RESTAction getAction() {
		return action;
	}

	public boolean isSuccess() {
		return success;
	}

	public ArrayList<RESTError> getErrors() {
		return errors;
	}

	public T getObject() {
		return object;
	}

	public HashMap<String, String> getMeta() {
		return meta;
	}

	public void setMeta(HashMap<String, String> meta) {
		this.meta = meta;
	}

}
