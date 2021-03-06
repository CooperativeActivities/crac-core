package crac;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import com.fasterxml.jackson.databind.JsonMappingException;

import crac.enums.ErrorCode;
import crac.enums.RESTAction;
import crac.exception.InvalidActionException;
import crac.exception.NoSuchStateException;
import crac.exception.RequirementsNotFullfilledException;
import crac.exception.SubItemsNotReadyException;
import crac.exception.InvalidParameterException;
import crac.module.utility.JSONResponseHelper;

/**
 * This controller provides handing of uncatched exceptions
 * Exceptions can be thrown by controllers and caught by this bean
 * Exceptions that get caught by standard methods have to be overwritten (example: handleHttpRequestMethodNotSupported) or else they will be ambiguous
 * @author David Hondl
 *
 */
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		return JSONResponseHelper.createResponseObj(RESTAction.ANY, "bad_request", ErrorCode.UNSUPPORTED_MEDIA_TYPE);
	}
	
	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		RESTAction action = RESTAction.GET;
		String m = ex.getMethod();

		if (m.equals("GET")) {
			action = RESTAction.GET;
		} else if (m.equals("POST")) {
			action = RESTAction.POST;
		} else if (m.equals("PUT")) {
			action = RESTAction.PUT;
		} else if (m.equals("DELETE")) {
			action = RESTAction.DELETE;
		}

		return JSONResponseHelper.createResponseObj(action, "bad_request", ErrorCode.WRONG_REQUEST_METHOD);
	}

	@Override
	protected ResponseEntity<Object> handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		return JSONResponseHelper.createResponseObj(RESTAction.ANY, "bad_request", ErrorCode.NO_SUCH_METHOD);
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		return JSONResponseHelper.createResponseObj(RESTAction.ANY, "bad_request", ErrorCode.NO_SUCH_TYPE);
	}

	@ExceptionHandler(InvalidParameterException.class)
	protected ResponseEntity<String> handleParameterException(Exception ex) {
		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.WRONG_PARAMETER);
	}
	
	@ExceptionHandler(FileNotFoundException.class)
	protected ResponseEntity<String> handleFileNotFoundException(Exception ex) {
		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.FILE_NOT_FOUND);
	}
	
	@ExceptionHandler(FileSizeLimitExceededException.class)
	protected ResponseEntity<String> handleFileSizeLimitExceededException(Exception ex) {
		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.FILESIZE_TOO_BIG);
	}

	@ExceptionHandler(NoNodeAvailableException.class)
	protected ResponseEntity<String> handleESExceptions(NoNodeAvailableException ex) {
		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ELASTICSEARCH_NOT_AVAILABLE);
	}

	@ExceptionHandler(JsonMappingException.class)
	protected ResponseEntity<String> handleJsonMappingException(Exception ex) {
		ex.printStackTrace();
		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.JSON_MAP_ERROR);
	}

	@ExceptionHandler(IOException.class)
	protected ResponseEntity<String> handleIOException(Exception ex) {
		ex.printStackTrace();
		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.JSON_READ_ERROR);
	}

	@ExceptionHandler(NoSuchStateException.class)
	protected ResponseEntity<String> handleNoSuchStateException(Exception ex) {
		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.NO_SUCH_STATE_FOUND);
	}

	@ExceptionHandler(RequirementsNotFullfilledException.class)
	protected ResponseEntity<String> handleRequirementsNotFullfilledException(Exception ex) {
		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.REQUIREMENTS_NOT_FULLFILLED);
	}

	@ExceptionHandler(SubItemsNotReadyException.class)
	protected ResponseEntity<String> handleSubItemsNotReadyException(Exception ex) {
		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.SUB_ITEMS_NOT_READY);
	}

	@ExceptionHandler(InvalidActionException.class)
	protected ResponseEntity<String> handleInvalidActionException(InvalidActionException ex) {
		return JSONResponseHelper.createResponse(false, "bad_request", ex.getError());
	}

}