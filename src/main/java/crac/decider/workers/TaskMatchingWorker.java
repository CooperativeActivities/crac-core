package crac.decider.workers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import crac.daos.TaskDAO;
import crac.decider.core.CracFilter;
import crac.decider.core.MatrixFilterConfiguration;
import crac.decider.core.UserFilterParameters;
import crac.decider.core.Worker;
import crac.decider.filter.UserRelationFilter;
import crac.decider.workers.config.GlobalMatrixConfig;
import crac.enums.TaskState;
import crac.models.CracUser;
import crac.models.Task;
import crac.models.relation.UserTaskRel;
import crac.models.storage.CompetenceCollectionMatrix;
import crac.models.utility.EvaluatedTask;

public class TaskMatchingWorker extends Worker {

	private CracUser user;
	private TaskDAO taskDAO;
	private UserFilterParameters up;

	public TaskMatchingWorker(CracUser u, TaskDAO taskDAO, UserFilterParameters up) {
		super();
		this.up = up;
		this.user = u;
		this.taskDAO = taskDAO;
	}

	public ArrayList<EvaluatedTask> run() {

		ArrayList<EvaluatedTask> tasks = new ArrayList<EvaluatedTask>();
		ArrayList<EvaluatedTask> remove = new ArrayList<EvaluatedTask>();
		CompetenceCollectionMatrix ccm;

		// load a filtered amount of tasks
		ArrayList<Task> filteredTaskSet = loadFilteredTask();

		// load the filters for matrix matching
		MatrixFilterConfiguration filters = GlobalMatrixConfig.cloneConfiguration();

		// add user-filters to the global filters
		addUserFilters(filters);

		for (Task t : filteredTaskSet) {
			if (t.getMappedCompetences() != null) {
				if (t.getMappedCompetences().size() != 0) {
					ccm = new CompetenceCollectionMatrix(user, t, filters);
					ccm.print();
					EvaluatedTask et = new EvaluatedTask(t, ccm.calcMatch());
					et.setDoable(ccm.isDoable());
					tasks.add(et);
				}
			}
		}

		if (tasks != null) {
			for (EvaluatedTask t : tasks) {
				if (!t.isDoable() || t.getAssessment() == 0) {
					remove.add(t);
				}
			}

			for (EvaluatedTask t : remove) {
				tasks.remove(t);
			}

			Collections.sort(tasks);
		}

		return tasks;
	}

	public void addUserFilters(MatrixFilterConfiguration m) {
		if (up.getFriends() == 1) {
			m.addFilter(new UserRelationFilter());
		}
	}

	public String getWorkerId() {
		return super.getWorkerId();
	}

	public ArrayList<Task> loadFilteredTask() {

		ArrayList<Task> result = new ArrayList<>();

		List<Task> found = taskDAO.findByTaskStateNot(TaskState.NOT_PUBLISHED);
		for (Task task : found) {
			if (task.isLeaf()) {
				boolean isConnected = false;
				for (UserTaskRel utr : task.getUserRelationships()) {
					if (utr.getUser().getId() == user.getId()) {
						isConnected = true;
					}
				}
				if (!isConnected) {
					result.add(task);
				}
			}
		}
		return result;
	}

}
