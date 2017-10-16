package crac.module.matching.interfaces;

import crac.module.matching.superclass.ConcreteFilter;

public interface FilterConfiguration {
	
	public void addFilter(ConcreteFilter filter);

	public void clearFilters();
	
	public FilterConfiguration clone();
	
	public void printFilters();
	
	public String filtersToString();
	
	public void restore();
	
    public int amount();
    
}
