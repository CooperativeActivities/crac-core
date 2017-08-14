package crac;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import com.fasterxml.jackson.databind.JsonMappingException;

import crac.enums.ErrorCause;
import crac.enums.RESTAction;
import crac.exception.NoSuchStateException;
import crac.exception.RequirementsNotFullfilledException;
import crac.exception.SubItemsNotReadyException;
import crac.exception.WrongParameterException;
import crac.module.utility.JSONResponseHelper;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		RESTAction action = RESTAction.GET;
		String m = ex.getMethod();
		
		if(m.equals("GET")){
			action = RESTAction.GET;
		}else if(m.equals("POST")){
			action = RESTAction.POST;
		}else if(m.equals("PUT")){
			action = RESTAction.PUT;
		}else if(m.equals("DELETE")){
			action = RESTAction.DELETE;
		}
		
		return JSONResponseHelper.createResponseObj(action, "bad_request", ErrorCause.WRONG_REQUEST_METHOD);
	}
	
	@Override
	protected ResponseEntity<Object> handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		return JSONResponseHelper.createResponseObj(RESTAction.ANY, "bad_request", ErrorCause.NO_SUCH_METHOD);
	}
	
	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		return JSONResponseHelper.createResponseObj(RESTAction.ANY, "bad_request", ErrorCause.NO_SUCH_TYPE);
	}
		
    @ExceptionHandler(WrongParameterException.class)
    protected ResponseEntity<String> handleParameterException(Exception ex) {
    	return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.WRONG_PARAMETER);
    }

    @ExceptionHandler(JsonMappingException.class)
    protected ResponseEntity<String> handleJsonMappingException(Exception ex) {
    	return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
    }
    
    @ExceptionHandler(IOException.class)
    protected ResponseEntity<String> handleIOException(Exception ex) {
    	return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
    }
    
    @ExceptionHandler(NoSuchStateException.class)
    protected ResponseEntity<String> handleNoSuchStateException(Exception ex) {
    	return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.NO_SUCH_STATE_FOUND);
    }

    @ExceptionHandler(RequirementsNotFullfilledException.class)
    protected ResponseEntity<String> handleRequirementsNotFullfilledException(Exception ex) {
    	return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.REQUIREMENTS_NOT_FULLFILLED);
    }

    @ExceptionHandler(SubItemsNotReadyException.class)
    protected ResponseEntity<String> handleSubItemsNotReadyException(Exception ex) {
    	return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.SUB_ITEMS_NOT_READY);
    }
    

}