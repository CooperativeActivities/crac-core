package crac.module.matching.superclass;

import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.interfaces.CracFilter;

public abstract class ConcreteFilter implements CracFilter {
	
	private String name;
	
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
