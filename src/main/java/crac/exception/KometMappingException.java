package crac.exception;

import crac.enums.ErrorCode;
import lombok.Getter;

public class KometMappingException extends Exception {

	private static final long serialVersionUID = 4980695608450807332L;

	public KometMappingException(){
        super();
    }

    public KometMappingException(String message){
        super(message);
    }
}
