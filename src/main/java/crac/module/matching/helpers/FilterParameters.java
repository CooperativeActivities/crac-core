package crac.module.matching.helpers;

import java.util.List;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;

public class FilterParameters {
	
	private List<Task> tasksPool;

	private List<EvaluatedTask> evaluatedTasksPool;

	private CracUser user;
	
	private MatrixField m;

	public FilterParameters() {
	}

	public List<Task> getTasksPool() {
		return tasksPool;
	}

	public void setTasksPool(List<Task> tasksPool) {
		this.tasksPool = tasksPool;
	}

	public List<EvaluatedTask> getEvaluatedTasksPool() {
		return evaluatedTasksPool;
	}

	public void setEvaluatedTasksPool(List<EvaluatedTask> evaluatedTasksPool) {
		this.evaluatedTasksPool = evaluatedTasksPool;
	}

	public CracUser getUser() {
		return user;
	}

	public void setUser(CracUser user) {
		this.user = user;
	}

	public MatrixField getM() {
		return m;
	}

	public void setM(MatrixField m) {
		this.m = m;
	}

}
