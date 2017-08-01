package crac.module.matching.superclass;

import crac.models.storage.MatrixField;
import crac.module.matching.interfaces.CracFilter;

public abstract class CracMatchingFilter implements CracFilter<Double, MatrixField> {
	
	private String name;
	
	public CracMatchingFilter(String name) {
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
	public abstract Double apply(MatrixField m);
		
}
