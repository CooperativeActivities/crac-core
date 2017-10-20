package crac.exception;

/**
 * This exception is thrown if defined subitems of an entity are not ready (for a state-change etc)
 * @author David Hondl
 *
 */
public class SubItemsNotReadyException extends RuntimeException {

	private static final long serialVersionUID = 2175282360567012053L;

	public SubItemsNotReadyException(){
        super();
    }

    public SubItemsNotReadyException(String message){
        super(message);
    }
}
