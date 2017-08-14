package crac.exception;

public class RequirementsNotFullfilledException extends RuntimeException {

	private static final long serialVersionUID = 1602648392574379893L;

	public RequirementsNotFullfilledException(){
        super();
    }

    public RequirementsNotFullfilledException(String message){
        super(message);
    }
}
