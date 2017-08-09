package crac.module.matching.superclass;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import crac.models.utility.PersonalizedFilter;
import crac.module.matching.factories.CracFilterFactory;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.interfaces.CracFilter;
import lombok.Getter;
import lombok.Setter;

public abstract class ConcreteFilter implements CracFilter {
	
	private String name;
	
	@Getter
	@Setter
	@JsonIgnore
	private PersonalizedFilter pf;
	
	@Getter
	@Setter
	@JsonIgnore
	private CracFilterFactory cff;
	
	public ConcreteFilter(String name) {
		this.name = name;
		System.out.println(name+" ready!");
}

	public void addSpeak(){
		System.out.println(name + " has been added!");
	}
	
	public void speak(){
		System.out.println(name + " is available!");
	}
	
	public String speakString(){
		return name + " is available!";
	}

	@Override
	public abstract void apply(FilterParameters fp);
		
}
