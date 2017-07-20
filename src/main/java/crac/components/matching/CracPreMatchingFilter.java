package crac.components.matching;

import java.util.List;

import crac.models.db.entities.Task;

public abstract class CracPreMatchingFilter implements CracFilter<List<Task>, List<Task>> {
	
	private String name;
	
	public CracPreMatchingFilter(String name) {
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
	public abstract List<Task> apply(List<Task> l);
		
}
