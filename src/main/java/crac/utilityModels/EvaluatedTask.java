package crac.utilityModels;

import crac.models.Task;

public class EvaluatedTask {
	
	private Task task;
	
	private double assessment;

	public EvaluatedTask(Task task, double assessment) {
		this.task = task;
		this.assessment = assessment;
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
	
}
