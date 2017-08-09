package crac.exception;

public class WrongParameterException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7751867800525916730L;

	public WrongParameterException(){
        super();
    }

    public WrongParameterException(String message){
        super(message);
    }
}
