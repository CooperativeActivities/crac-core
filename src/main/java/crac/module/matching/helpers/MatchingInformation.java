package crac.module.matching.helpers;

import java.util.List;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import lombok.Data;

@Data
public class MatchingInformation {
	
	private List<Task> tasks;
	private CracUser user;
	public MatchingInformation(List<Task> tasks, CracUser user) {
		this.tasks = tasks;
		this.user = user;
	}
}
