package crac.module.utility.filter.input;

import java.util.List;

import crac.module.matching.superclass.ConcreteFilter;
import lombok.Data;

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
}