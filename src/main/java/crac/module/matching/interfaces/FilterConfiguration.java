package crac.module.matching.interfaces;

import crac.module.matching.superclass.ConcreteFilter;

/**
 * Marks a class as filter-configuration
 * @author David Hondl
 *
 */
public interface FilterConfiguration {
	
	/**
	 * Consumes target ConcreteFilter and adds it to the configuration
	 * @param filter
	 */
	public void addFilter(ConcreteFilter filter);

	/**
	 * Clears the filters of the configuration
	 */
	public void clearFilters();
	
	/**
	 * Clones the configuration and its filters
	 * @return FilterConfiguration
	 */
	public FilterConfiguration clone();
	
	/**
	 * Prints the configuration and its filters
	 */
	public void printFilters();
	
	/**
	 * Returns the configurations and its filters as string
	 * @return String
	 */
	public String filtersToString();
	
	/**
	 * Restores the configuration to its original state
	 */
	public void restore();
	
	/**
	 * Returns the amount of filters stored by the configuration
	 * @return int
	 */
    public int amount();
    
}
