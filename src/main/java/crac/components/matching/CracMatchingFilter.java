package crac.components.matching;

import crac.models.storage.MatrixField;

public abstract class CracMatchingFilter implements CracFilter<Double, MatrixField> {
	
	private String name;
	
	public CracMatchingFilter(String name) {
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

	@Override
	public abstract Double apply(MatrixField m);
		
}
