package crac.models.output;

import java.util.ArrayList;
import java.util.HashMap;

import crac.enums.ErrorCode;
import crac.enums.RESTAction;

public class RESTResponse<T> {
	
	private String type;
	private RESTAction rest_action;
	private boolean success;
	private ArrayList<RESTError> errors;
	private T object;
	private HashMap<String, Object> meta;
	
	public RESTResponse(RESTAction rest_action, boolean success, T object) {
		type = "NO_OBJECT";
		this.object = null;
		if(object != null){
			type = object.getClass().getSimpleName();
			this.object = object;
		}
		this.rest_action = rest_action;
		this.success = success;
		this.errors = new ArrayList<>();
		this.meta = new HashMap<>();
	}
	
	public void addError(ErrorCode name, String cause){
		errors.add(new RESTError(name, cause));
	}

	public String getType() {
		return type;
	}

	public RESTAction getRest_action() {
		return rest_action;
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

	public HashMap<String, Object> getMeta() {
		return meta;
	}

	public void setMeta(HashMap<String, Object> meta) {
		this.meta = meta;
	}
	
	public class RESTError {
		
		private ErrorCode name;
		private String cause;

		public RESTError(ErrorCode name, String cause) {
			this.name = name;
			this.cause = cause;
		}
		
		public ErrorCode getName() {
			return name;
		}
		public String getCause() {
			return cause;
		}
	}

}
