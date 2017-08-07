package crac.models.utility;

import java.util.List;

import crac.module.matching.superclass.ConcreteFilter;
import lombok.Getter;
import lombok.Setter;

public class PersonalizedFilter {
	
	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private List<ParamterDummy> params;
	
	@Getter
	@Setter
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
