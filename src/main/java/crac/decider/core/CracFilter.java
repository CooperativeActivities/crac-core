package crac.decider.core;

import crac.models.storage.MatrixField;

public abstract class CracFilter {
	
	private String name;
	
	public CracFilter(String name) {
		this.name = name;
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

	public abstract double apply(MatrixField m);
		
}
