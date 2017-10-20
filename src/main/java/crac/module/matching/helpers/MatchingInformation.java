package crac.module.matching.helpers;

import java.util.List;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import lombok.Data;

/**
 * Helperclass that contains a list of tasks and target user
 * Used to lower the amount of parameters for the apply()-method of matching-filters
 * @author David Hondl
 *
 */
@Data
public class MatchingInformation {
	
	private List<Task> tasks;
	private CracUser user;
	public MatchingInformation(List<Task> tasks, CracUser user) {
		this.tasks = tasks;
		this.user = user;
	}
}
