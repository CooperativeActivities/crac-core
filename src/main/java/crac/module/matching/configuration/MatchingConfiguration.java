package crac.module.matching.configuration;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import crac.module.matching.helpers.MatrixField;
import crac.module.matching.interfaces.CracFilter;
import crac.module.matching.interfaces.FilterConfiguration;
import crac.module.matching.superclass.CracMatchingFilter;

@Service
@Scope("singleton")
public class MatchingConfiguration implements FilterConfiguration {
	
	private ArrayList<CracMatchingFilter> filters;
	
	public MatchingConfiguration(){
		filters = new ArrayList<>();
}

	public void applyFilters(MatrixField m){
		for(CracMatchingFilter filter : filters){
			m.setVal(filter.apply(m));
		}
	}
	
	@Override
	public void addFilter(CracFilter<?, ?> filter) {
		filters.add((CracMatchingFilter) filter);		
	}
	
	@Override
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
	
	@Override
	public FilterConfiguration clone(){
		MatchingConfiguration m = new MatchingConfiguration();
		for(CracMatchingFilter filter : filters){
			m.addFilter(filter);
		}
		return m;
	}


}
