package crac.module.matching.configuration;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.interfaces.FilterConfiguration;
import crac.module.matching.superclass.ConcreteFilter;

@Service
@Scope("singleton")
public class MatchingConfiguration implements FilterConfiguration {
	
	private ArrayList<ConcreteFilter> filters;
	
	public MatchingConfiguration(){
		filters = new ArrayList<>();
}

	public void applyFilters(FilterParameters fp){
		for(ConcreteFilter filter : filters){
			filter.apply(fp);
		}
	}
	
	@Override
	public void addFilter(ConcreteFilter filter) {
		filters.add(filter);		
	}
	
	@Override
	public void clearFilters(){
		filters.clear();
	}
	
	public void printFilters(){
		for(ConcreteFilter filter : filters){
			filter.speak();
		}
	}
	
	public String filtersToString(){
		String s = "";
		for(ConcreteFilter filter : filters){
			s += filter.speakString() + " ";
		}
		return s;
	}
	
	@Override
	public FilterConfiguration clone(){
		MatchingConfiguration m = new MatchingConfiguration();
		for(ConcreteFilter filter : filters){
			m.addFilter(filter);
		}
		return m;
	}


}
