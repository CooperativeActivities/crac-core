package crac.models.input;

import java.util.ArrayList;
import java.util.List;

import crac.module.factories.CracFilterFactory;
import crac.module.matching.interfaces.FilterConfiguration;
import crac.module.matching.superclass.ConcreteFilter;
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
    
    /**
     * Helper-class that contains a single filter
     * @author David
     *
     */
    @Data
    public class PersonalizedFilter {
    	
    	private String name;
    	private List<InputParameters> params;
    	private ConcreteFilter cf;
    	
    	public PersonalizedFilter(){
    		
    	}
    	
    	public Object getParam(String name){
    		for(InputParameters pd : params){
    			if(pd.getName().equals(name)){
    				return pd.getValue();
    			}
    		}
    		return null;
    	}
    	
    	public boolean paramExists(String name){
    		for(InputParameters pd : params){
    			if(pd.getName().equals(name)){
    				return true;
    			}
    		}
    		return false;
    	}
    	
    	/**
    	 * Helper class that contains attributes of a single filter
    	 * @author David
    	 *
    	 */
    	@Data
    	public class InputParameters {
    		
    		private String name;
    		private Object value;

    	}
    	
    }


}
