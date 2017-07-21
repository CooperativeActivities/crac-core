package crac.components.matching;

import java.util.ArrayList;
import java.util.List;

import crac.components.matching.interfaces.CracFilter;
import crac.models.db.entities.Task;
import crac.models.utility.EvaluatedTask;

public abstract class CracPostMatchingFilter implements CracFilter<ArrayList<EvaluatedTask>, ArrayList<EvaluatedTask>> {
	
	private String name;
	
	public CracPostMatchingFilter(String name) {
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
	public abstract ArrayList<EvaluatedTask> apply(ArrayList<EvaluatedTask> list);
		
}
