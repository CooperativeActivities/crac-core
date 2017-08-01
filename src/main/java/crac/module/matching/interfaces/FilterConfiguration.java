package crac.module.matching.interfaces;

public interface FilterConfiguration {
	
	public void addFilter(CracFilter<?, ?> filter);

	public void clearFilters();
	
	public FilterConfiguration clone();
	
}
