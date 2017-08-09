package crac;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.JsonMappingException;

import crac.enums.ErrorCause;
import crac.exception.WrongParameterException;
import crac.module.utility.JSONResponseHelper;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
 
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
    
}