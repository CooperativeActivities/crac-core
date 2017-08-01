package crac.module.matching.configuration;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import crac.module.matching.interfaces.CracFilter;
import crac.module.matching.interfaces.FilterConfiguration;
import crac.module.matching.superclass.CracPostMatchingFilter;

@Service
@Scope("singleton")
public class PostMatchingConfiguration implements FilterConfiguration {

	private ArrayList<CracPostMatchingFilter> filters;

	public PostMatchingConfiguration() {
		filters = new ArrayList<>();
}

	@Override
	public void clearFilters() {
		filters.clear();
	}

	public ArrayList<CracPostMatchingFilter> getFilters() {
		return filters;
	}

	@Override
	public void addFilter(CracFilter<?, ?> filter) {
		filters.add((CracPostMatchingFilter) filter);		
	}
	
	@Override
	public FilterConfiguration clone(){
		return this;
	}

}
