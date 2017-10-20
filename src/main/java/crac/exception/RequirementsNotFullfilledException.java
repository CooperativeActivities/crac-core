package crac.exception;

/**
 * This exception is thrown if defined requirements in the framework are not fullfilled
 * @author David Hondl
 *
 */
public class RequirementsNotFullfilledException extends RuntimeException {

	private static final long serialVersionUID = 1602648392574379893L;

	public RequirementsNotFullfilledException(){
        super();
    }

    public RequirementsNotFullfilledException(String message){
        super(message);
    }
}
