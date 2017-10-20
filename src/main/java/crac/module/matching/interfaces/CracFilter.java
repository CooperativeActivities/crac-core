package crac.module.matching.interfaces;

import crac.module.matching.helpers.FilterParameters;

/**
 * Interface marking target class as a filter, used in filter-configurations
 * @author David Hondl
 *
 */
public interface CracFilter {
	
	/**
	 * Apply method, that modifies values, based on the given filter-parameters
	 * @param fp
	 */
	public void apply(FilterParameters fp);

}
