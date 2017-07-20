package crac.components.matching.configuration;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import crac.components.matching.CracFilter;
import crac.components.matching.CracPreMatchingFilter;
import crac.components.matching.filter.prematching.GroupFilter;

@Service
@Scope("singleton")
public class PreMatchingConfiguration {

	private ArrayList<CracPreMatchingFilter> filters;

	public PreMatchingConfiguration() {
		filters = new ArrayList<>();
		filters.add(new GroupFilter());
	}

	public void addFilter(CracPreMatchingFilter filter) {
		filters.add(filter);
	}

	public void emptyFilters() {
		filters.clear();
	}

	public ArrayList<CracPreMatchingFilter> getFilters() {
		return filters;
	}

}
