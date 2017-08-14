package crac.module.utility;

import java.util.HashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import crac.enums.ErrorCause;
import crac.enums.RESTAction;
import crac.models.output.RESTResponse;

public class JSONResponseHelper {

	// Create Responses with different parameters

	public static <T> ResponseEntity<String> createResponse(boolean success, String cause, ErrorCause error) {
		return createResponse(null, success, cause, error, null, RESTAction.GET);
	}

	public static <T> ResponseEntity<String> createResponse(T obj, boolean success) {
		return createResponse(obj, success, null, null, null, RESTAction.GET);
	}

	public static <T> ResponseEntity<String> createResponse(T obj, boolean success, RESTAction action) {
		return createResponse(obj, success, null, null, null, action);
	}

	public static <T> ResponseEntity<String> createResponse(boolean success, HashMap<String, Object> meta) {
		return createResponse(null, success, null, null, meta, RESTAction.GET);
	}

	public static <T> ResponseEntity<String> createResponse(boolean success, HashMap<String, Object> meta,
			RESTAction action) {
		return createResponse(null, success, null, null, meta, action);
	}

	public static <T> ResponseEntity<String> createResponse(T obj, boolean success, HashMap<String, Object> meta) {
		return createResponse(obj, success, null, null, meta, RESTAction.GET);
	}

	public static <T> ResponseEntity<String> createResponse(T obj, boolean success, HashMap<String, Object> meta,
			RESTAction action) {
		return createResponse(obj, success, null, null, meta, action);
	}

	public static <T> ResponseEntity<String> createResponse(boolean success, String cause, ErrorCause error,
			HashMap<String, Object> meta) {
		return createResponse(null, success, cause, error, meta, RESTAction.GET);
	}

	public static <T> ResponseEntity<String> createResponse(boolean success, String cause, ErrorCause error,
			RESTAction action) {
		return createResponse(null, success, cause, error, null, action);
	}

	public static <T> ResponseEntity<String> createResponse(T obj, RESTAction action) {
		return createResponse(obj, true, null, null, null, action);
	}

	// REST-Change Helpers

	public static <T> ResponseEntity<String> successfullyCreated(T obj) {
		return createResponse(obj, RESTAction.CREATE);
	}

	public static <T> ResponseEntity<String> successfullyDeleted(T obj) {
		return createResponse(obj, RESTAction.DELETE);
	}

	public static <T> ResponseEntity<String> successfullyUpdated(T obj) {
		return createResponse(obj, RESTAction.UPDATE);
	}

	public static <T> ResponseEntity<String> successfullyAssigned(T obj) {
		return createResponse(obj, RESTAction.GET);
	}

	// Method that creates the response

	public static <T> ResponseEntity<String> createResponse(T obj, boolean success, String cause, ErrorCause error,
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
			return createResponse(false, "bad_request", ErrorCause.JSON_WRITE_ERROR);
		}

		return addEntity(result, success);

	}

	// Configure the response

	private static ResponseEntity<String> addEntity(String response, boolean status) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		if (status) {
			return ResponseEntity.ok().headers(headers).body(response);
		} else {
			return ResponseEntity.badRequest().headers(headers).body(response);
		}

	}

	public static <T> ResponseEntity<Object> createResponseObj(RESTAction action, String cause, ErrorCause error) {
		
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
