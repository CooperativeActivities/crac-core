package crac.module.matching.configuration;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import crac.module.matching.filter.matching.ImportancyLevelFilter;
import crac.module.matching.filter.matching.LikeLevelFilter;
import crac.module.matching.filter.matching.ProficiencyLevelFilter;
import crac.module.matching.filter.matching.UserRelationFilter;
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
	public FilterConfiguration clone(){
		MatchingConfiguration m = new MatchingConfiguration();
		filters.forEach(m::addFilter);
		return m;
	}

	@Override
	public void restore() {
		this.clearFilters();
		filters.add(new ProficiencyLevelFilter());
		filters.add(new LikeLevelFilter());
		filters.add(new ImportancyLevelFilter());
		filters.add(new UserRelationFilter());
	}

    @Override
    public int amount() {
        return filters.size();
    }

}
