package crac.models.utility;

import java.util.List;

import crac.module.matching.superclass.ConcreteFilter;
import lombok.Data;

@Data
public class PersonalizedFilter {
	
	private String name;
	private List<ParamterDummy> params;
	private ConcreteFilter cf;
	
	public PersonalizedFilter(){
		
	}
	
	public Object getParam(String name){
		for(ParamterDummy pd : params){
			if(pd.getName().equals(name)){
				return pd.getValue();
			}
		}
		return null;
	}
	
	public boolean paramExists(String name){
		for(ParamterDummy pd : params){
			if(pd.getName().equals(name)){
				return true;
			}
		}
		return false;
	}
	
}
