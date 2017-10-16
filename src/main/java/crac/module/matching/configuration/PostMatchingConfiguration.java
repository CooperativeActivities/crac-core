package crac.module.matching.configuration;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import crac.module.matching.filter.matching.ImportancyLevelFilter;
import crac.module.matching.filter.matching.LikeLevelFilter;
import crac.module.matching.filter.matching.ProficiencyLevelFilter;
import crac.module.matching.filter.matching.UserRelationFilter;
import crac.module.matching.filter.postmatching.ClearFilter;
import crac.module.matching.filter.postmatching.MissingVolunteerFilter;
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
	public FilterConfiguration clone() {
		return this;
	}

	@Override
	public void printFilters() {
		filters.forEach(ConcreteFilter::speak);
	}

	@Override
	public String filtersToString() {
		String s = "";
		try {
			s = filters.stream().map(filter -> filter.speakString()).reduce((f1, f2) -> f1 + f2 + " ").get();
		} catch (NoSuchElementException ex) {
			s = "No filter available!";
		}
		return s;
	}

	@Override
	public void restore() {
		this.clearFilters();
		filters.add(new ClearFilter());
		filters.add(new MissingVolunteerFilter());
	}

    @Override
    public int amount() {
        return filters.size();
    }

}
