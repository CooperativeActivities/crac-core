package crac.components.matching;

import java.util.List;

import crac.components.matching.interfaces.CracFilter;
import crac.models.db.entities.Task;
import crac.models.utility.MatchingInformation;

public abstract class CracPreMatchingFilter implements CracFilter<List<Task>, MatchingInformation> {
	
	private String name;
	
	public CracPreMatchingFilter(String name) {
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
	public abstract List<Task> apply(MatchingInformation mi);
		
}