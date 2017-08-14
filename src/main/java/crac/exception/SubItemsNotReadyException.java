package crac.exception;

public class SubItemsNotReadyException extends RuntimeException {

	private static final long serialVersionUID = 2175282360567012053L;

	public SubItemsNotReadyException(){
        super();
    }

    public SubItemsNotReadyException(String message){
        super(message);
    }
}
