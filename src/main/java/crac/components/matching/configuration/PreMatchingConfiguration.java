package crac.components.matching.configuration;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import crac.components.matching.CracPreMatchingFilter;
import crac.components.matching.filter.prematching.GroupFilter;
import crac.components.matching.filter.prematching.LoggedUserFilter;
import crac.components.matching.interfaces.CracFilter;
import crac.components.matching.interfaces.FilterConfiguration;

@Service
@Scope("singleton")
public class PreMatchingConfiguration implements FilterConfiguration {

	private ArrayList<CracPreMatchingFilter> filters;

	public PreMatchingConfiguration() {
		filters = new ArrayList<>();
	}

	@Override
	public void clearFilters() {
		filters.clear();
	}

	public ArrayList<CracPreMatchingFilter> getFilters() {
		return filters;
	}

	@Override
	public void addFilter(CracFilter<?, ?> filter) {
		filters.add((CracPreMatchingFilter) filter);		
	}
	
	@Override
	public FilterConfiguration clone(){
		return this;
	}

}
