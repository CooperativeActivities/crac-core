package crac.exception;

/**
 * This exceptions is thrown if given parameters are invalid
 * @author David Hondl
 *
 */
public class InvalidParameterException extends RuntimeException {

	private static final long serialVersionUID = -7751867800525916730L;

	public InvalidParameterException(){
        super();
    }

    public InvalidParameterException(String message){
        super(message);
    }
}
