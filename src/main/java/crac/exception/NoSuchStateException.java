package crac.exception;

public class NoSuchStateException extends RuntimeException {

	private static final long serialVersionUID = -2025919460280300540L;

	public NoSuchStateException(){
        super();
    }

    public NoSuchStateException(String message){
        super(message);
    }
}
