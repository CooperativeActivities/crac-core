package crac.models.output;

import java.util.ArrayList;
import java.util.HashMap;

import crac.enums.ErrorCode;
import crac.enums.RESTAction;
import lombok.Getter;
import lombok.Setter;

/**
 * Helper class for arbitrary output, that contains information about the whole request
 * This is the output-class for the framework, each response ends up beeing an instance of this class
 * @author David Hondl
 *
 * @param <T>
 */
public class RESTResponse<T> {
	
	@Getter
	private String type;
	@Getter
	private RESTAction rest_action;
	@Getter
	private boolean success;
	@Getter
	private ArrayList<RESTError> errors;
	@Getter
	private T object;
	@Getter
	@Setter
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
	
	public class RESTError {
		
		@Getter
		private ErrorCode name;
		@Getter
		private String cause;

		public RESTError(ErrorCode name, String cause) {
			this.name = name;
			this.cause = cause;
		}
		
	}

}
