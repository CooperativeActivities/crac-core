package crac.module.matching.interfaces;

/**
 * Marks target class as something that is the outcoming datatype of a synchronisation between komet and crac
 * @author David Hondl
 *
 */
public interface SyncableCrac {
	
	/**
	 * Method that marks the object as deprecated
	 * @param deprecated
	 */
	void setDeprecated(boolean deprecated);
	
	/**
	 * Method to extract the id of the object
	 * @return long
	 */
	long getId();

}
