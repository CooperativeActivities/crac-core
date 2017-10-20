package crac.module.matching.helpers;

import java.util.List;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import lombok.Data;

/**
 * Helperclass that contains references to a task-pool, an evaluated task-pool, target user and a matrix-field
 * Used to lower the amount of parameters for the apply()-method of matching-filters
 * @author David Hondl
 *
 */
@Data
public class FilterParameters {
	
	private List<Task> tasksPool;
	private List<EvaluatedTask> evaluatedTasksPool;
	private CracUser user;
	private MatrixField m;

	public FilterParameters() {
	}

}
