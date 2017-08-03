package crac.module.matching.helpers;

import java.util.List;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import lombok.Getter;
import lombok.Setter;

public class FilterParameters {
	
	@Getter
	@Setter
	private List<Task> tasksPool;

	@Getter
	@Setter
	private List<EvaluatedTask> evaluatedTasksPool;

	@Getter
	@Setter
	private CracUser user;
	
	@Getter
	@Setter
	private MatrixField m;

	public FilterParameters() {
	}

}
