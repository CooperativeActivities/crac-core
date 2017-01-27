package crac.decider.core;

import java.util.ArrayList;

import crac.models.storage.MatrixField;

public class FilterConfiguration {
	
	private ArrayList<CracFilter> filters = new ArrayList<>();

	public void applyFilters(MatrixField m){
		for(CracFilter filter : filters){
			m.setVal(filter.apply(m));
		}
	}
	
	public void addFilter(CracFilter filter){
		filters.add(filter);
	}
	
	public void clearFilters(){
		filters.clear();
	}
	
	public void printFilters(){
		for(CracFilter filter : filters){
			filter.speak();
		}
	}
	
	public String filtersToString(){
		String s = "";
		for(CracFilter filter : filters){
			s += filter.speakString() + " ";
		}
		return s;
	}
	
	public FilterConfiguration clone(){
		FilterConfiguration m = new FilterConfiguration();
		for(CracFilter filter : filters){
			m.addFilter(filter);
		}
		return m;
	}

}
