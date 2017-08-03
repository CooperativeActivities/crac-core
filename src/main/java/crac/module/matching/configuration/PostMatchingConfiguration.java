package crac.module.matching.configuration;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import crac.module.matching.interfaces.FilterConfiguration;
import crac.module.matching.superclass.ConcreteFilter;

@Service
@Scope("singleton")
public class PostMatchingConfiguration implements FilterConfiguration {

	private ArrayList<ConcreteFilter> filters;

	public PostMatchingConfiguration() {
		filters = new ArrayList<>();
}

	@Override
	public void clearFilters() {
		filters.clear();
	}

	public ArrayList<ConcreteFilter> getFilters() {
		return filters;
	}

	@Override
	public void addFilter(ConcreteFilter filter) {
		filters.add(filter);		
	}
	
	@Override
	public FilterConfiguration clone(){
		return this;
	}

}
