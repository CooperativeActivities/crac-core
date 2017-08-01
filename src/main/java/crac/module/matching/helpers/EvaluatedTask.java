package crac.module.matching.helpers;

import crac.models.db.entities.Task;

public class EvaluatedTask implements Comparable{
	
	private Task task;
	
	private double assessment;
	
	private boolean doable;

	public EvaluatedTask(Task task, double assessment) {
		this.task = task;
		this.assessment = assessment;
		this.doable = true;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public double getAssessment() {
		return assessment;
	}

	public void setAssessment(double assessment) {
		this.assessment = assessment;
	}

	public boolean isDoable() {
		return doable;
	}

	public void setDoable(boolean doable) {
		this.doable = doable;
	}

	@Override
	public int compareTo(Object task) {
		if(((EvaluatedTask) task).getAssessment() > this.getAssessment()){
			return 1;
		}else if(((EvaluatedTask) task).getAssessment() == this.getAssessment()){
			return 0;
		}else{
			return -1;
		}
	}
	
}
