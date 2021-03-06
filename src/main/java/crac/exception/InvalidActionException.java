package crac.exception;

import crac.enums.ErrorCode;
import lombok.Getter;

/**
 * An excpetion that represents an arbitrary exceptions and contains an ErrorCode
 * @author David Hondl
 *
 */
public class InvalidActionException extends Exception {

	private static final long serialVersionUID = -4886026371101053264L;
	
	@Getter
	private ErrorCode error;

	public InvalidActionException(){
        super();
        error = ErrorCode.ACTION_NOT_VALID;
    }

    public InvalidActionException(ErrorCode error){
        this.error = error;
    }
}
