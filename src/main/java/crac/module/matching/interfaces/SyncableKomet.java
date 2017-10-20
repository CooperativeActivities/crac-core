package crac.module.matching.interfaces;

import java.util.Map;

import org.springframework.data.repository.CrudRepository;

import crac.exception.KometMappingException;

/**
 * Marks target class as something that is the incoming datatype of a synchronisation between komet and crac
 * @author David Hondl
 *
 */
public interface SyncableKomet {

	/**
	 * Method that maps this object to a SyncableCrac-object
	 * @param map
	 * @return SyncableCrac
	 * @throws KometMappingException
	 */
	public SyncableCrac map(Map<Class<?>, CrudRepository<?, ?>> map)  throws KometMappingException;

	/**
	 * Returns the valid-status of the object
	 * @return boolean
	 */
	public boolean isValid();

	/**
	 * Method to extract the id of the object
	 * @return int
	 */
	public int getUid();

}
