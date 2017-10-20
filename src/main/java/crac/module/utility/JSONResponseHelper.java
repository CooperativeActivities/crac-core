package crac.module.utility;

import java.util.HashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import crac.enums.ErrorCode;
import crac.enums.RESTAction;
import crac.models.output.RESTResponse;

/**
 * Class that contains static methods for creating JSON-output for the framework
 * @author David Hondl
 *
 */
public class JSONResponseHelper {

	// Create Responses with different parameters

	/**
	 * Accepts different parameters and creates a JSON-response from them by calling the overloaded createResponse()-method
	 * @param success
	 * @param cause
	 * @param error
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> createResponse(boolean success, String cause, ErrorCode error) {
		return createResponse(null, success, cause, error, null, RESTAction.GET);
	}

	/**
	 * Accepts different parameters and creates a JSON-response from them by calling the overloaded createResponse()-method
	 * @param obj
	 * @param success
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> createResponse(T obj, boolean success) {
		return createResponse(obj, success, null, null, null, RESTAction.GET);
	}

	/**
	 * Accepts different parameters and creates a JSON-response from them by calling the overloaded createResponse()-method
	 * @param obj
	 * @param success
	 * @param action
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> createResponse(T obj, boolean success, RESTAction action) {
		return createResponse(obj, success, null, null, null, action);
	}

	/**
	 * Accepts different parameters and creates a JSON-response from them by calling the overloaded createResponse()-method
	 * @param success
	 * @param meta
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> createResponse(boolean success, HashMap<String, Object> meta) {
		return createResponse(null, success, null, null, meta, RESTAction.GET);
	}

	/**
	 * Accepts different parameters and creates a JSON-response from them by calling the overloaded createResponse()-method
	 * @param success
	 * @param meta
	 * @param action
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> createResponse(boolean success, HashMap<String, Object> meta,
			RESTAction action) {
		return createResponse(null, success, null, null, meta, action);
	}

	/**
	 * Accepts different parameters and creates a JSON-response from them by calling the overloaded createResponse()-method
	 * @param obj
	 * @param success
	 * @param meta
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> createResponse(T obj, boolean success, HashMap<String, Object> meta) {
		return createResponse(obj, success, null, null, meta, RESTAction.GET);
	}

	/**
	 * Accepts different parameters and creates a JSON-response from them by calling the overloaded createResponse()-method
	 * @param obj
	 * @param success
	 * @param meta
	 * @param action
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> createResponse(T obj, boolean success, HashMap<String, Object> meta,
			RESTAction action) {
		return createResponse(obj, success, null, null, meta, action);
	}

	/**
	 * Accepts different parameters and creates a JSON-response from them by calling the overloaded createResponse()-method
	 * @param success
	 * @param cause
	 * @param error
	 * @param meta
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> createResponse(boolean success, String cause, ErrorCode error,
			HashMap<String, Object> meta) {
		return createResponse(null, success, cause, error, meta, RESTAction.GET);
	}

	/**
	 * Accepts different parameters and creates a JSON-response from them by calling the overloaded createResponse()-method
	 * @param success
	 * @param cause
	 * @param error
	 * @param action
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> createResponse(boolean success, String cause, ErrorCode error,
			RESTAction action) {
		return createResponse(null, success, cause, error, null, action);
	}

	/**
	 * Accepts different parameters and creates a JSON-response from them by calling the overloaded createResponse()-method
	 * @param obj
	 * @param action
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> createResponse(T obj, RESTAction action) {
		return createResponse(obj, true, null, null, null, action);
	}

	// REST-Change Helpers

	/**
	 * Method that takes in a newly created object and generates a create-message as JSON-output
	 * @param obj
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> successfullyCreated(T obj) {
		return createResponse(obj, RESTAction.CREATE);
	}

	/**
	 * Method that takes in a newly created object and generates a delete-message as JSON-output
	 * @param obj
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> successfullyDeleted(T obj) {
		return createResponse(obj, RESTAction.DELETE);
	}

	/**
	 * Method that takes in a newly created object and generates a update-message as JSON-output
	 * @param obj
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> successfullyUpdated(T obj) {
		return createResponse(obj, RESTAction.UPDATE);
	}

	/**
	 * Method that takes in a newly created object and generates an assign-message as JSON-output
	 * @param obj
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> successfullyAssigned(T obj) {
		return createResponse(obj, RESTAction.GET);
	}

	// Method that creates the response

	/**
	 * Actual createResponse()-method that gets called by overriding-methods and creates the RESTResponse-object that gets converted into JSON
	 * @param obj
	 * @param success
	 * @param cause
	 * @param error
	 * @param meta
	 * @param action
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<String> createResponse(T obj, boolean success, String cause, ErrorCode error,
			HashMap<String, Object> meta, RESTAction action) {

		RESTResponse<T> r = new RESTResponse<T>(action, success, obj);

		if (error != null && cause != null) {
			r.addError(error, cause);
		}

		if (meta != null) {
			r.setMeta(meta);
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		String result = "";
		try {
			result = mapper.writeValueAsString(r);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return createResponse(false, "bad_request", ErrorCode.JSON_WRITE_ERROR);
		}

		return addEntity(result, success);

	}

	// Configure the response

	/**
	 * Configures the response-entity
	 * @param response
	 * @param status
	 * @return ResponseEntity<String>
	 */
	private static ResponseEntity<String> addEntity(String response, boolean status) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		if (status) {
			return ResponseEntity.ok().headers(headers).body(response);
		} else {
			return ResponseEntity.badRequest().headers(headers).body(response);
		}

	}

	/**
	 * Creates and returns the final response-object
	 * @param action
	 * @param cause
	 * @param error
	 * @return ResponseEntity<String>
	 */
	public static <T> ResponseEntity<Object> createResponseObj(RESTAction action, String cause, ErrorCode error) {
		
		RESTResponse<T> r = new RESTResponse<T>(action, false, null);
		
		if (error != null && cause != null) {
			r.addError(error, cause);
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		String result = "";
		try {
			result = mapper.writeValueAsString(r);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return ResponseEntity.badRequest().headers(headers).body(result);
	}

}
