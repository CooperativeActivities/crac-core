package crac.models.input;

import java.util.ArrayList;
import java.util.List;

import crac.module.factories.CracFilterFactory;
import crac.module.matching.interfaces.FilterConfiguration;
import crac.module.matching.superclass.ConcreteFilter;
import crac.module.utility.filter.input.PersonalizedFilter;
import lombok.Data;

/**
 * Helperclass that maps json-input
 * The collected data then is used to generate the concrete filters and use them for filtering tasks
 * 
 * This class also contains inner classes for structuring the data further
 * @author David Hondl
 *
 */
@Data
public class PersonalizedFilters {

	private String query;
	private List<PersonalizedFilter> filters;

	public PersonalizedFilters() {
		query = "";
		filters = new ArrayList<>();
	}

	public void convert(CracFilterFactory mf, String path) {
		
		filters.forEach( filter -> {
			ConcreteFilter cf = mf.createMatchingFilterFromString(filter.getName(), path);
			cf.setPf(filter);
			filter.setCf(cf);
		});

	}

    public boolean convertAndAdd(CracFilterFactory mf, FilterConfiguration matchingConfig, String path){
        filters.forEach( filter -> {
            ConcreteFilter cf = mf.createMatchingFilterFromString(filter.getName(), path);
            cf.setPf(filter);
            matchingConfig.addFilter(cf);
        });
        return true;
    }
    
}
