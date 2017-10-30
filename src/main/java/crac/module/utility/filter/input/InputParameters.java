package crac.module.utility.filter.input;

import crac.models.input.PersonalizedFilters;
import lombok.Data;

/**
 * Helper class that contains attributes of a single filter
 * @author David
 *
 */

@Data
public class InputParameters {
	
	public InputParameters(){
		
	}
	
	private String name;
	private Object value;

}