package crac.module.matching.helpers;

import java.util.List;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;

public class MatchingInformation {
	
	private List<Task> tasks;
	private CracUser user;
	public MatchingInformation(List<Task> tasks, CracUser user) {
		this.tasks = tasks;
		this.user = user;
	}
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	public CracUser getUser() {
		return user;
	}
	public void setUser(CracUser user) {
		this.user = user;
	}

}
