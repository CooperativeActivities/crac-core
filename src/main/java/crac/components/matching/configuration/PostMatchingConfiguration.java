package crac.components.matching.configuration;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import crac.components.matching.CracPostMatchingFilter;
import crac.components.matching.interfaces.CracFilter;
import crac.components.matching.interfaces.FilterConfiguration;

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
