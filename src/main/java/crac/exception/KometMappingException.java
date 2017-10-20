package crac.exception;

/**
 * This exceptions represents and error while mapping a SyncableKomet-Entity
 * @author David Hondl
 *
 */
public class KometMappingException extends Exception {

	private static final long serialVersionUID = 4980695608450807332L;

	public KometMappingException(){
        super();
    }

    public KometMappingException(String message){
        super(message);
    }
}
