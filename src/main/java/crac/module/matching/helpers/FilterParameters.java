package crac.module.matching.helpers;

import java.util.List;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import lombok.Data;

@Data
public class FilterParameters {
	
	private List<Task> tasksPool;
	private List<EvaluatedTask> evaluatedTasksPool;
	private CracUser user;
	private MatrixField m;

	public FilterParameters() {
	}

}
