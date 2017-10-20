package crac.module.matching.configuration;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import crac.module.matching.filter.matching.ImportancyLevelFilter;
import crac.module.matching.filter.matching.LikeLevelFilter;
import crac.module.matching.filter.matching.ProficiencyLevelFilter;
import crac.module.matching.filter.matching.UserRelationFilter;
import crac.module.matching.filter.prematching.GroupFilter;
import crac.module.matching.filter.prematching.LoggedUserFilter;
import crac.module.matching.interfaces.FilterConfiguration;
import crac.module.matching.superclass.ConcreteFilter;

/**
 * This singleton-component provides a configuration for filtering the tasks before the competence-based onthology-matching has been done
 * It can be accessed and re-configured globally
 * @author David Hondl
 *
 */
@Service
@Scope("singleton")
public class PreMatchingConfiguration implements FilterConfiguration {

	private ArrayList<ConcreteFilter> filters;

	public PreMatchingConfiguration() {
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
	
	@Override
	public void printFilters(){
		filters.forEach(ConcreteFilter::speak);
	}
	
	@Override
	public String filtersToString(){	
		return filters.stream()
				.map( filter -> filter.speakString() )
				.reduce( (f1, f2) -> f1 + f2 + " " )
				.get();
	}
	
	@Override
	public void restore() {
		this.clearFilters();
		filters.add(new LoggedUserFilter());
		filters.add(new GroupFilter());
	}

    @Override
    public int amount() {
        return filters.size();
    }

}
