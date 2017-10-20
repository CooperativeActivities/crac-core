package crac.module.matching.interfaces;

/**
 * Functional-interface that marks a class as something, that either has an error or not
 * Can be implemented via lambda
 * @author David Hondl
 *
 */
@FunctionalInterface
public interface ErrorStatus {
	
	/**
	 * Returns the error-status of an object
	 * @return boolean
	 */
	public boolean hasError();
		
}
