package crac.exception;

/**
 * This exception represents a not-existing state (that is called)
 * @author David Hondl
 *
 */
public class NoSuchStateException extends RuntimeException {

	private static final long serialVersionUID = -2025919460280300540L;

	public NoSuchStateException(){
        super();
    }

    public NoSuchStateException(String message){
        super(message);
    }
}
