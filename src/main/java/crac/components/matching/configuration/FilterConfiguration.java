package crac.components.matching.configuration;

import java.util.ArrayList;

import crac.components.matching.CracMatchingFilter;
import crac.models.storage.MatrixField;

public class FilterConfiguration {
	
	private ArrayList<CracMatchingFilter> filters = new ArrayList<>();

	public void applyFilters(MatrixField m){
		for(CracMatchingFilter filter : filters){
			m.setVal(filter.apply(m));
		}
	}
	
	public void addFilter(CracMatchingFilter filter){
		filters.add(filter);
	}
	
	public void clearFilters(){
		filters.clear();
	}
	
	public void printFilters(){
		for(CracMatchingFilter filter : filters){
			filter.speak();
		}
	}
	
	public String filtersToString(){
		String s = "";
		for(CracMatchingFilter filter : filters){
			s += filter.speakString() + " ";
		}
		return s;
	}
	
	public FilterConfiguration clone(){
		FilterConfiguration m = new FilterConfiguration();
		for(CracMatchingFilter filter : filters){
			m.addFilter(filter);
		}
		return m;
	}

}
