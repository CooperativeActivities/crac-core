package crac.module.matching.configuration;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import crac.module.matching.filter.prematching.GroupFilter;
import crac.module.matching.filter.prematching.LoggedUserFilter;
import crac.module.matching.interfaces.CracFilter;
import crac.module.matching.interfaces.FilterConfiguration;
import crac.module.matching.superclass.CracPreMatchingFilter;

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
